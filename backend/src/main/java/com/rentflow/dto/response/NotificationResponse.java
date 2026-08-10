package com.rentflow.dto.response;

import com.rentflow.entity.enums.NotificationChannel;
import com.rentflow.entity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID tenancyId;
    private UUID unitId;
    private String title;
    private String message;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String recipientAddress;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private Instant createdAt;
    private Instant updatedAt;
}
