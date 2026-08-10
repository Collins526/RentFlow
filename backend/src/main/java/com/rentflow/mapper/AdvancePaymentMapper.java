package com.rentflow.mapper;

import com.rentflow.dto.response.AdvancePaymentResponse;
import com.rentflow.entity.AdvancePayment;

public class AdvancePaymentMapper {
    private AdvancePaymentMapper() {}

    public static AdvancePaymentResponse toDto(AdvancePayment a) {
        if (a == null) return null;
        return AdvancePaymentResponse.builder()
                .id(a.getId())
                .organizationId(a.getOrganizationId())
                .tenantId(a.getTenantId())
                .leaseId(a.getLeaseId())
                .amount(a.getAmount())
                .remainingAmount(a.getRemainingAmount())
                .status(a.getStatus())
                .note(a.getNote())
                .receivedDate(a.getReceivedDate())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
