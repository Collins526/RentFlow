package com.rentflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SecurityDepositRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    private String note;
    private LocalDate receivedDate;
}
