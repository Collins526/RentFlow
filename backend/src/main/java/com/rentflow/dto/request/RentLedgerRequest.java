package com.rentflow.dto.request;

import com.rentflow.entity.enums.LedgerEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RentLedgerRequest {
    private UUID tenancyId;
    private UUID leaseId;
    private UUID invoiceId;
    private UUID paymentId;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID unitId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotNull(message = "Entry type is required")
    private LedgerEntryType type;

    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    private String description;
}
