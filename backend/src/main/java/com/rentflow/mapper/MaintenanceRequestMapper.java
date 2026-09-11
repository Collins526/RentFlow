package com.rentflow.mapper;

import com.rentflow.dto.response.MaintenanceRequestResponse;
import com.rentflow.entity.MaintenanceRequest;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRequestMapper {

    public MaintenanceRequestResponse toResponse(MaintenanceRequest entity) {
        if (entity == null) {
            return null;
        }

        return MaintenanceRequestResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .tenantId(entity.getTenantId())
                .unitId(entity.getUnitId())
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
