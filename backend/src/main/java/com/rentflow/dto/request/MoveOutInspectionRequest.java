package com.rentflow.dto.request;

import com.rentflow.entity.enums.MoveOutInspectionStatus;
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
public class MoveOutInspectionRequest {
    private UUID organizationId;
    private UUID tenantId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;
    private LocalDate inspectionDate;
    private String inspectorName;
    private MoveOutInspectionStatus status;
    private String findings;
}
