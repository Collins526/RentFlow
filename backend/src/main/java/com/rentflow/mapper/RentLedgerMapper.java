package com.rentflow.mapper;

import com.rentflow.dto.response.RentLedgerResponse;
import com.rentflow.entity.RentLedgerEntry;

public class RentLedgerMapper {

    private RentLedgerMapper() {}

    public static RentLedgerResponse toDto(RentLedgerEntry entry) {
        if (entry == null) return null;

        return RentLedgerResponse.builder()
                .id(entry.getId())
                .organizationId(entry.getOrganizationId())
                .tenancyId(entry.getTenancyId())
                .leaseId(entry.getLeaseId())
                .invoiceId(entry.getInvoiceId())
                .paymentId(entry.getPaymentId())
                .tenantId(entry.getTenantId())
                .unitId(entry.getUnitId())
                .entryDate(entry.getEntryDate())
                .type(entry.getType())
                .description(entry.getDescription())
                .amount(entry.getAmount())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
