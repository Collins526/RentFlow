package com.rentflow.dto.request;

import com.rentflow.entity.enums.MoveInInspectionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MoveInInspectionRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    private String inspectorName;
    private MoveInInspectionStatus status;
    private String findings;
}
