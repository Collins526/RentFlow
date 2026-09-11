package com.rentflow.service.impl;

import com.rentflow.config.MpesaProperties;
import com.rentflow.dto.response.MpesaPaymentResponse;
import com.rentflow.entity.Payment;
import com.rentflow.entity.enums.PaymentMethod;
import com.rentflow.entity.enums.PaymentStatus;
import com.rentflow.exception.BadRequestException;
import com.rentflow.repository.PaymentRepository;
import com.rentflow.repository.RentInvoiceRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.MpesaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MpesaServiceImpl implements MpesaService {

    private static final Logger log = LoggerFactory.getLogger(MpesaServiceImpl.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MpesaProperties mpesaProperties;
    private final PaymentRepository paymentRepository;
    private final RentInvoiceRepository rentInvoiceRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public MpesaPaymentResponse initiateStkPush(String phoneNumber,
                                               BigDecimal amount,
                                               String accountReference,
                                               String transactionDesc,
                                               UUID tenantId,
                                               UUID invoiceId,
                                               UUID unitId,
                                               String reference) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("M-Pesa phone number is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("M-Pesa amount must be greater than zero");
        }
        if (tenantId == null) {
            throw new BadRequestException("Tenant ID is required for M-Pesa payments");
        }

        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (invoiceId != null) {
            rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(invoiceId, organizationId)
                    .orElseThrow(() -> new BadRequestException("Invoice not found"));
        }

        Payment payment = Payment.builder()
                .organizationId(organizationId)
                .tenantId(tenantId)
                .unitId(unitId)
                .invoiceId(invoiceId)
                .amount(amount)
                .method(PaymentMethod.MPESA)
                .status(PaymentStatus.PENDING)
                .reference(reference != null ? reference : accountReference)
                .phoneNumber(normalizedPhoneNumber)
                .paymentDate(LocalDate.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        try {
            validateConfiguration();

            String accessToken = fetchAccessToken();
            String timestamp = TIMESTAMP_FORMATTER.format(ZonedDateTime.now());
            String password = encodePassword(mpesaProperties.getShortCode(), mpesaProperties.getPasskey(), timestamp);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("BusinessShortCode", mpesaProperties.getShortCode());
            payload.put("Password", password);
            payload.put("Timestamp", timestamp);
            payload.put("TransactionType", "CustomerPayBillOnline");
            payload.put("Amount", amount);
            payload.put("PartyA", normalizedPhoneNumber);
            payload.put("PartyB", mpesaProperties.getShortCode());
            payload.put("PhoneNumber", normalizedPhoneNumber);
            payload.put("CallBackURL", mpesaProperties.getCallbackUrl());
            payload.put("AccountReference", accountReference != null ? accountReference : mpesaProperties.getAccountReference());
            payload.put("TransactionDesc", transactionDesc != null ? transactionDesc : mpesaProperties.getTransactionDesc());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestUrl = trimTrailingSlash(mpesaProperties.getBaseUrl()) + "/mpesa/stkpush/v1/processrequest";

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(requestUrl, HttpMethod.POST,
                    new HttpEntity<>(payload, headers), new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = response.getBody();

            if (body == null) {
                savedPayment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(savedPayment);
                throw new BadRequestException("M-Pesa response was empty");
            }

            String responseCode = Objects.toString(body.get("ResponseCode"), "");
            String checkoutRequestId = Objects.toString(body.get("CheckoutRequestID"), null);
            String responseDescription = Objects.toString(body.get("ResponseDescription"), "");

            boolean success = "0".equals(responseCode);
            if (!success) {
                savedPayment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(savedPayment);
                throw new BadRequestException("M-Pesa request failed: " + responseDescription);
            }

            savedPayment.setExternalReference(checkoutRequestId);
            paymentRepository.save(savedPayment);

            return MpesaPaymentResponse.builder()
                    .checkoutRequestId(checkoutRequestId)
                    .responseCode(responseCode)
                    .responseDescription(responseDescription)
                    .success(true)
                    .build();
        } catch (BadRequestException ex) {
            if (savedPayment.getId() != null) {
                savedPayment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(savedPayment);
            }
            throw ex;
        } catch (Exception ex) {
            if (savedPayment.getId() != null) {
                savedPayment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(savedPayment);
            }
            throw new BadRequestException("M-Pesa payment could not be initiated: " + ex.getMessage());
        }
    }

    @Override
    public void processCallback(Map<String, Object> callbackPayload) {
        if (callbackPayload == null || callbackPayload.isEmpty()) {
            throw new BadRequestException("M-Pesa callback payload is empty");
        }

        Map<String, Object> stkCallback = extractStkCallback(callbackPayload);
        String checkoutRequestId = Objects.toString(stkCallback.get("CheckoutRequestID"), null);
        if (checkoutRequestId == null || checkoutRequestId.isBlank()) {
            throw new BadRequestException("Invalid M-Pesa callback payload: missing CheckoutRequestID");
        }

        int resultCode = parseInteger(stkCallback.get("ResultCode"));
        String resultDesc = Objects.toString(stkCallback.get("ResultDesc"), null);

        paymentRepository.findByExternalReferenceAndDeletedAtIsNull(checkoutRequestId)
                .ifPresent(payment -> {
                    payment.setStatus(resultCode == 0 ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
                    if (resultCode == 0) {
                        payment.setPaidAt(Instant.now());
                    }
                    paymentRepository.save(payment);
                    if (resultCode == 0 && payment.getInvoiceId() != null) {
                        rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(
                                        payment.getInvoiceId(), payment.getOrganizationId())
                                .ifPresent(invoice -> {
                                    invoice.setStatus(com.rentflow.entity.enums.InvoiceStatus.PAID);
                                    rentInvoiceRepository.save(invoice);
                                });
                    }
                    log.info("Updated payment {} from M-Pesa callback: resultCode={}, resultDesc={}", payment.getId(), resultCode, resultDesc);
                });
    }

    private String fetchAccessToken() {
        String tokenUrl = trimTrailingSlash(mpesaProperties.getBaseUrl()) + "/oauth/v1/generate?grant_type=client_credentials";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(mpesaProperties.getConsumerKey(), mpesaProperties.getConsumerSecret(), StandardCharsets.UTF_8);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenUrl, HttpMethod.GET,
                    new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("access_token")) {
                throw new BadRequestException("Failed to acquire M-Pesa access token");
            }

            return Objects.toString(body.get("access_token"), "");
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            throw new BadRequestException("M-Pesa OAuth failed (HTTP " + ex.getStatusCode().value()
                    + "): " + (responseBody.isBlank() ? "empty response from Daraja" : responseBody));
        }
    }

    private int parseInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(value, "0"));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Map<String, Object> extractStkCallback(Map<String, Object> payload) {
        if (payload.containsKey("stkCallback") && payload.get("stkCallback") instanceof Map<?, ?>) {
            return toStringKeyMap(payload.get("stkCallback"));
        }

        if (payload.containsKey("Body") && payload.get("Body") instanceof Map<?, ?> body) {
            Map<String, Object> bodyMap = toStringKeyMap(body);
            if (bodyMap.containsKey("stkCallback") && bodyMap.get("stkCallback") instanceof Map<?, ?>) {
                return toStringKeyMap(bodyMap.get("stkCallback"));
            }
        }

        if (payload.containsKey("ResultCode") || payload.containsKey("CheckoutRequestID") || payload.containsKey("ResultDesc")) {
            return payload;
        }

        throw new BadRequestException("Invalid M-Pesa callback payload: missing stkCallback data");
    }

    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> map) {
            return toStringKeyMap(map);
        }
        throw new BadRequestException("Invalid M-Pesa callback payload: missing or malformed " + key);
    }

    private Map<String, Object> toStringKeyMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new BadRequestException("Invalid M-Pesa callback payload: expected a map");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String keyName) {
                result.put(keyName, entry.getValue());
            }
        }
        return result;
    }

    private String encodePassword(String shortCode, String passkey, String timestamp) {
        String value = shortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private void validateConfiguration() {
        if (isMissingOrPlaceholder(mpesaProperties.getBaseUrl())) {
            throw new IllegalStateException("M-Pesa base URL is not configured");
        }
        if (isMissingOrPlaceholder(mpesaProperties.getConsumerKey())) {
            throw new IllegalStateException("M-Pesa consumer key is not configured");
        }
        if (isMissingOrPlaceholder(mpesaProperties.getConsumerSecret())) {
            throw new IllegalStateException("M-Pesa consumer secret is not configured");
        }
        if (isMissingOrPlaceholder(mpesaProperties.getShortCode())) {
            throw new IllegalStateException("M-Pesa short code is not configured");
        }
        if (isMissingOrPlaceholder(mpesaProperties.getPasskey())) {
            throw new IllegalStateException("M-Pesa passkey is not configured");
        }
        if (isMissingOrPlaceholder(mpesaProperties.getCallbackUrl())) {
            throw new IllegalStateException("M-Pesa callback URL is not configured");
        }
    }

    private boolean isMissingOrPlaceholder(String value) {
        return value == null || value.isBlank() || (value.startsWith("<") && value.endsWith(">"));
    }

    private String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.trim().replaceAll("[\\s()-]", "");

        if (normalized.startsWith("+254")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("0")) {
            normalized = "254" + normalized.substring(1);
        }

        if (!normalized.matches("254[17]\\d{8}")) {
            throw new BadRequestException("M-Pesa phone number must be a valid Kenyan number, e.g. 0712345678");
        }

        return normalized;
    }
}
