package com.rentflow.mapper;

import com.rentflow.dto.response.RentInvoiceResponse;
import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;

public class RentInvoiceMapper {

    private RentInvoiceMapper() {}

    public static RentInvoiceResponse toDto(RentInvoice invoice, Tenant tenant, Unit unit) {
        if (invoice == null) return null;

        return RentInvoiceResponse.builder()
                .id(invoice.getId())
                .organizationId(invoice.getOrganizationId())
                .tenancyId(invoice.getTenancyId())
                .leaseId(invoice.getLeaseId())
                .tenantId(invoice.getTenantId())
                .tenantName(TenancyMapper.displayName(tenant))
                .unitId(invoice.getUnitId())
                .unitNumber(unit != null ? unit.getUnitNumber() : "Unknown unit")
                .propertyId(unit != null ? unit.getPropertyId() : null)
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
