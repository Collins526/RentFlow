package com.rentflow.dto.response;

import com.rentflow.entity.enums.MoveInInspectionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MoveInInspectionResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;
    private LocalDate inspectionDate;
    private String inspectorName;
    private MoveInInspectionStatus status;
    private String findings;
    private Instant createdAt;
    private Instant updatedAt;
}
