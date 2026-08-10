package com.rentflow.dto.response;

import com.rentflow.entity.enums.MaintenancePriority;
import com.rentflow.entity.enums.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequestResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID unitId;
    private UUID tenancyId;
    private String title;
    private String description;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private LocalDate requestedDate;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String assignedTo;
    private Instant createdAt;
    private Instant updatedAt;
}
