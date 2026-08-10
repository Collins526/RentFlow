package com.rentflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AdvanceAllocationRequest {

    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private BigDecimal amount;
}
