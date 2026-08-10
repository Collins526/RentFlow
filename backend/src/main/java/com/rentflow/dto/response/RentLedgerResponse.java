package com.rentflow.dto.response;

import com.rentflow.entity.enums.LedgerEntryType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RentLedgerResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID invoiceId;
    private UUID paymentId;
    private UUID tenantId;
    private UUID unitId;
    private LocalDate entryDate;
    private LedgerEntryType type;
    private String description;
    private BigDecimal amount;
    private Instant createdAt;
    private Instant updatedAt;
}
