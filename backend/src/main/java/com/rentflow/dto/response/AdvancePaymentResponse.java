package com.rentflow.dto.response;

import com.rentflow.entity.enums.AdvanceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AdvancePaymentResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID leaseId;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private AdvanceStatus status;
    private String note;
    private LocalDate receivedDate;
    private Instant createdAt;
    private Instant updatedAt;
}
