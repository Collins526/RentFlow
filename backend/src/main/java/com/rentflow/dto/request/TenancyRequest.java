package com.rentflow.dto.request;

import com.rentflow.entity.enums.TenancyStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TenancyRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /** Null means open-ended (month to month). */
    private LocalDate endDate;

    @NotNull(message = "Status is required")
    private TenancyStatus status;

    /** Falls back to the unit's rent when omitted. */
    @DecimalMin(value = "0.0", message = "Rent amount cannot be negative")
    private BigDecimal rentAmount;

    /** Falls back to the unit's deposit when omitted. */
    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    private BigDecimal securityDepositAmount;
}
