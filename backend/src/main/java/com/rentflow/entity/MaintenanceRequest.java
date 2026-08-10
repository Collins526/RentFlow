package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.MaintenancePriority;
import com.rentflow.entity.enums.MaintenanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "tenancy_id")
    private UUID tenancyId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MaintenancePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MaintenanceStatus status;

    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "assigned_to", length = 150)
    private String assignedTo;
}
