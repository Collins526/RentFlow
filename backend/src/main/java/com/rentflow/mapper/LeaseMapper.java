package com.rentflow.mapper;

import com.rentflow.dto.response.LeaseResponse;
import com.rentflow.entity.Lease;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;

public class LeaseMapper {

    private LeaseMapper() {
    }

    public static LeaseResponse toDto(Lease lease, Tenant tenant, Unit unit) {
        if (lease == null) {
            return null;
        }

        return LeaseResponse.builder()
                .id(lease.getId())
                .organizationId(lease.getOrganizationId())
                .tenancyId(lease.getTenancyId())
                .tenantId(lease.getTenantId())
                .tenantName(TenancyMapper.displayName(tenant))
                .unitId(lease.getUnitId())
                .unitNumber(unit != null ? unit.getUnitNumber() : "Unknown unit")
                .propertyId(unit != null ? unit.getPropertyId() : null)
                .startDate(lease.getStartDate())
                .endDate(lease.getEndDate())
                .status(lease.getStatus())
                .rentAmount(lease.getRentAmount())
                .securityDepositAmount(lease.getSecurityDepositAmount())
                .terms(lease.getTerms())
                .createdAt(lease.getCreatedAt())
                .updatedAt(lease.getUpdatedAt())
                .build();
    }
}
