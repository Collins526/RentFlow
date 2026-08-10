package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.MoveInInspectionStatus;
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
@Table(name = "move_in_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveInInspection extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "tenancy_id")
    private UUID tenancyId;

    @Column(name = "lease_id")
    private UUID leaseId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "inspector_name", length = 150)
    private String inspectorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MoveInInspectionStatus status;

    @Column(name = "findings", length = 2000)
    private String findings;
}
