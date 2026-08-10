package com.rentflow.mapper;

import com.rentflow.dto.response.TenancyResponse;
import com.rentflow.entity.Tenancy;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.TenantType;

public class TenancyMapper {

    private TenancyMapper() {
    }

    /**
     * Renders a tenant for display. Individuals are shown by person name, corporates
     * by company name, with a fallback so a soft-deleted counterpart never blanks out
     * a historical tenancy row.
     */
    public static String displayName(Tenant tenant) {
        if (tenant == null) {
            return "Unknown tenant";
        }

        if (tenant.getTenantType() == TenantType.CORPORATE) {
            return tenant.getCompanyName() != null ? tenant.getCompanyName() : "Unnamed company";
        }

        String name = ((tenant.getFirstName() != null ? tenant.getFirstName() : "") + " "
                + (tenant.getLastName() != null ? tenant.getLastName() : "")).trim();
        return name.isEmpty() ? "Unnamed tenant" : name;
    }

    public static TenancyResponse toDto(Tenancy tenancy, Tenant tenant, Unit unit) {
        if (tenancy == null) {
            return null;
        }

        return TenancyResponse.builder()
                .id(tenancy.getId())
                .organizationId(tenancy.getOrganizationId())
                .tenantId(tenancy.getTenantId())
                .tenantName(displayName(tenant))
                .unitId(tenancy.getUnitId())
                .unitNumber(unit != null ? unit.getUnitNumber() : "Unknown unit")
                .propertyId(unit != null ? unit.getPropertyId() : null)
                .startDate(tenancy.getStartDate())
                .endDate(tenancy.getEndDate())
                .status(tenancy.getStatus())
                .rentAmount(tenancy.getRentAmount())
                .securityDepositAmount(tenancy.getSecurityDepositAmount())
                .createdAt(tenancy.getCreatedAt())
                .updatedAt(tenancy.getUpdatedAt())
                .build();
    }
}
