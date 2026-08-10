package com.rentflow.service;

import com.rentflow.dto.request.AdvancePaymentRequest;
import com.rentflow.dto.response.AdvancePaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface AdvancePaymentService {
    AdvancePaymentResponse receiveAdvance(AdvancePaymentRequest request);
    AdvancePaymentResponse allocateAdvance(UUID advanceId, UUID invoiceId, BigDecimal amount);
    AdvancePaymentResponse getAdvanceById(UUID id);
    Page<AdvancePaymentResponse> listAdvances(UUID tenantId, Pageable pageable);
    void deleteAdvance(UUID id);
}
