package com.rentflow.mapper;

import com.rentflow.dto.response.MoveOutInspectionResponse;
import com.rentflow.entity.MoveOutInspection;
import org.springframework.stereotype.Component;

@Component
public class MoveOutInspectionMapper {

    public MoveOutInspectionResponse toResponse(MoveOutInspection entity) {
        if (entity == null) {
            return null;
        }

        return MoveOutInspectionResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .tenantId(entity.getTenantId())
                .tenancyId(entity.getTenancyId())
                .leaseId(entity.getLeaseId())
                .unitId(entity.getUnitId())
                .inspectionDate(entity.getInspectionDate())
                .inspectorName(entity.getInspectorName())
                .status(entity.getStatus())
                .findings(entity.getFindings())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
