package com.rentflow.mapper;

import com.rentflow.dto.response.MaintenanceRequestResponse;
import com.rentflow.entity.MaintenanceRequest;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceRequestMapper {

    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;

    public MaintenanceRequestResponse toResponse(MaintenanceRequest entity) {
        if (entity == null) {
            return null;
        }

        Tenant tenant = tenantRepository.findById(entity.getTenantId()).orElse(null);
        Unit unit = entity.getUnitId() != null ? unitRepository.findById(entity.getUnitId()).orElse(null) : null;

        return MaintenanceRequestResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .tenantId(entity.getTenantId())
            .tenantName(TenancyMapper.displayName(tenant))
                .unitId(entity.getUnitId())
            .unitNumber(unit != null ? unit.getUnitNumber() : null)
                .tenancyId(entity.getTenancyId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .attachmentData(entity.getAttachmentData())
                .attachmentName(entity.getAttachmentName())
                .attachmentType(entity.getAttachmentType())
                .attachmentSize(entity.getAttachmentSize())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .requestedDate(entity.getRequestedDate())
                .scheduledDate(entity.getScheduledDate())
                .completedDate(entity.getCompletedDate())
                .assignedTo(entity.getAssignedTo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
