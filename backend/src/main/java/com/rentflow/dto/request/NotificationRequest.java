package com.rentflow.dto.request;

import com.rentflow.entity.enums.NotificationChannel;
import com.rentflow.entity.enums.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private UUID tenantId;
    private UUID tenancyId;
    private UUID unitId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Status is required")
    private NotificationStatus status;

    private String recipientAddress;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
}
