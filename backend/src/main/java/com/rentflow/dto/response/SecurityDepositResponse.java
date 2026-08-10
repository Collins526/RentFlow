package com.rentflow.dto.response;

import com.rentflow.entity.enums.DepositStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SecurityDepositResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private DepositStatus status;
    private String note;
    private LocalDate receivedDate;
    private Instant createdAt;
    private Instant updatedAt;
}
