package com.rentflow.mapper;

import com.rentflow.dto.response.NotificationResponse;
import com.rentflow.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification entity) {
        if (entity == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .tenantId(entity.getTenantId())
                .tenancyId(entity.getTenancyId())
                .unitId(entity.getUnitId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .channel(entity.getChannel())
                .status(entity.getStatus())
                .recipientAddress(entity.getRecipientAddress())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
