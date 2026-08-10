package com.rentflow.service;

import com.rentflow.dto.request.PaymentRequest;
import com.rentflow.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentById(UUID id);
    Page<PaymentResponse> getPayments(UUID tenantId, UUID invoiceId, Pageable pageable);
    void deletePayment(UUID id);
}
