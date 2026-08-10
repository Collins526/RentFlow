package com.rentflow.dto.request;

import com.rentflow.entity.enums.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Create/update payload for a single unit.
 * <p>
 * {@code occupancyStatus} is intentionally excluded: occupancy is driven by
 * tenancies and can only be changed through the dedicated occupancy endpoint.
 */
@Data
public class UnitRequest {

    @NotBlank(message = "Unit number is required")
    @Size(max = 50, message = "Unit number cannot exceed 50 characters")
    private String unitNumber;

    @NotNull(message = "Unit type is required")
    private UnitType type;

    /** Optional block within the parent property. */
    private UUID blockId;

    private Integer floorNumber;

    @Min(value = 0, message = "Bedrooms cannot be negative")
    private Integer bedrooms = 0;

    @Min(value = 0, message = "Bathrooms cannot be negative")
    private Integer bathrooms = 0;

    @DecimalMin(value = "0.0", message = "Size cannot be negative")
    private BigDecimal sizeSqFt;

    @NotNull(message = "Rent amount is required")
    @DecimalMin(value = "0.0", message = "Rent amount cannot be negative")
    private BigDecimal rentAmount;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    private Boolean furnished = false;

    @Size(max = 50, message = "Water meter number cannot exceed 50 characters")
    private String waterMeterNumber;

    @Size(max = 50, message = "Electricity meter number cannot exceed 50 characters")
    private String electricityMeterNumber;

    private String description;

    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;
}
