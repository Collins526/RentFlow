package com.rentflow.dto.request;

import com.rentflow.entity.enums.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Generates a sequential run of near-identical units, e.g. prefix "A" from 101
 * for a count of 20 yields A101 through A120. All generated units share the
 * attributes declared here.
 */
@Data
public class BulkUnitRequest {

    @Size(max = 20, message = "Prefix cannot exceed 20 characters")
    private String prefix;

    @NotNull(message = "Start number is required")
    @Min(value = 0, message = "Start number cannot be negative")
    private Integer startNumber;

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 200, message = "Cannot create more than 200 units at once")
    private Integer count;

    /** Zero-pads the numeric portion, e.g. padding 3 turns 1 into "001". */
    @Min(value = 0, message = "Number padding cannot be negative")
    @Max(value = 10, message = "Number padding cannot exceed 10")
    private Integer numberPadding = 0;

    private UUID blockId;

    @NotNull(message = "Unit type is required")
    private UnitType type;

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

    private String description;
}
