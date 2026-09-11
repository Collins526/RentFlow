package com.rentflow.service.impl;

import com.rentflow.dto.request.PaymentRequest;
import com.rentflow.dto.response.PaymentResponse;
import com.rentflow.entity.Payment;
import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.enums.PaymentStatus;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.PaymentMapper;
import com.rentflow.repository.PaymentRepository;
import com.rentflow.repository.RentInvoiceRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentInvoiceRepository rentInvoiceRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null || !currentTenantId.equals(request.getTenantId())) {
                throw new UnauthorizedException("Tenant can only submit payments for their own tenancy");
            }
        }

        if (request.getInvoiceId() != null) {
            rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(request.getInvoiceId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        }

        Payment payment = Payment.builder()
                .organizationId(organizationId)
                .tenantId(request.getTenantId())
                .unitId(request.getUnitId())
                .invoiceId(request.getInvoiceId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.COMPLETED)
                .reference(request.getReference())
                .phoneNumber(request.getPhoneNumber())
                .externalReference(request.getReference())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paidAt(Instant.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        if (saved.getStatus() == PaymentStatus.COMPLETED) {
            settleInvoice(saved);
        }

        // TODO: create ledger entry and apply to invoice balance

        return PaymentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Payment p = paymentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null || !currentTenantId.equals(p.getTenantId())) {
                throw new UnauthorizedException("Tenant can only access their own payments");
            }
        }

        return PaymentMapper.toDto(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(UUID tenantId, UUID invoiceId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null) {
                throw new UnauthorizedException("Tenant is not linked to a tenant record");
            }
            if (tenantId != null && !currentTenantId.equals(tenantId)) {
                throw new UnauthorizedException("Tenant can only view their own payment history");
            }
            tenantId = currentTenantId;
            invoiceId = null;
        }

        Page<Payment> page;
        if (tenantId != null) {
            page = paymentRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (invoiceId != null) {
            page = paymentRepository.findByOrganizationIdAndInvoiceIdAndDeletedAtIsNull(organizationId, invoiceId, pageable);
        } else {
            page = paymentRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(PaymentMapper::toDto);
    }

    @Override
    @Transactional
    public void deletePayment(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Payment p = paymentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null || !currentTenantId.equals(p.getTenantId())) {
                throw new UnauthorizedException("Tenant can only delete their own payments");
            }
        }

        p.setDeletedAt(Instant.now());
        paymentRepository.save(p);
    }

    private void settleInvoice(Payment payment) {
        if (payment.getInvoiceId() == null) {
            return;
        }

        rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(
                        payment.getInvoiceId(), payment.getOrganizationId())
                .ifPresent(invoice -> {
                    invoice.setStatus(com.rentflow.entity.enums.InvoiceStatus.PAID);
                    rentInvoiceRepository.save(invoice);
                });
    }
}
