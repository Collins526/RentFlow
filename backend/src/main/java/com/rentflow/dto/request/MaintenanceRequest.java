package com.rentflow.dto.request;

import com.rentflow.entity.enums.MaintenancePriority;
import com.rentflow.entity.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID unitId;
    private UUID tenancyId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String attachmentData;
    private String attachmentName;
    private String attachmentType;
    private Long attachmentSize;

    @NotNull(message = "Priority is required")
    private MaintenancePriority priority;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status;

    private LocalDate requestedDate;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String assignedTo;
}
