package com.rentflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RentInvoiceRequest {
    private UUID tenancyId;
    private UUID leaseId;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Period start is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;

    private LocalDate dueDate;

    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    private String notes;
}
