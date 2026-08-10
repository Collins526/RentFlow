package com.rentflow.mapper;

import com.rentflow.dto.response.SecurityDepositResponse;
import com.rentflow.entity.SecurityDeposit;

public class SecurityDepositMapper {
    private SecurityDepositMapper() {}

    public static SecurityDepositResponse toDto(SecurityDeposit deposit) {
        if (deposit == null) return null;
        return SecurityDepositResponse.builder()
                .id(deposit.getId())
                .organizationId(deposit.getOrganizationId())
                .tenantId(deposit.getTenantId())
                .tenancyId(deposit.getTenancyId())
                .leaseId(deposit.getLeaseId())
                .unitId(deposit.getUnitId())
                .amount(deposit.getAmount())
                .remainingAmount(deposit.getRemainingAmount())
                .status(deposit.getStatus())
                .note(deposit.getNote())
                .receivedDate(deposit.getReceivedDate())
                .createdAt(deposit.getCreatedAt())
                .updatedAt(deposit.getUpdatedAt())
                .build();
    }
}
