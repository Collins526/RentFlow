package com.rentflow.mapper;

import com.rentflow.dto.response.PaymentResponse;
import com.rentflow.entity.Payment;

public class PaymentMapper {
    private PaymentMapper() {}

    public static PaymentResponse toDto(Payment p) {
        if (p == null) return null;
        return PaymentResponse.builder()
                .id(p.getId())
                .organizationId(p.getOrganizationId())
                .tenantId(p.getTenantId())
                .unitId(p.getUnitId())
                .invoiceId(p.getInvoiceId())
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .reference(p.getReference())
                .phoneNumber(p.getPhoneNumber())
                .externalReference(p.getExternalReference())
                .paymentDate(p.getPaymentDate())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
