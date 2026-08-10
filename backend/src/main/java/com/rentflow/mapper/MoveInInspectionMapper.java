package com.rentflow.mapper;

import com.rentflow.dto.response.MoveInInspectionResponse;
import com.rentflow.entity.MoveInInspection;

public class MoveInInspectionMapper {
    private MoveInInspectionMapper() {}

    public static MoveInInspectionResponse toDto(MoveInInspection inspection) {
        if (inspection == null) return null;
        return MoveInInspectionResponse.builder()
                .id(inspection.getId())
                .organizationId(inspection.getOrganizationId())
                .tenantId(inspection.getTenantId())
                .tenancyId(inspection.getTenancyId())
                .leaseId(inspection.getLeaseId())
                .unitId(inspection.getUnitId())
                .inspectionDate(inspection.getInspectionDate())
                .inspectorName(inspection.getInspectorName())
                .status(inspection.getStatus())
                .findings(inspection.getFindings())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }
}
