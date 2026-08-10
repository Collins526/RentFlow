package com.rentflow.dto.response;

import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.UnitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {
    private UUID id;
    private UUID propertyId;
    private UUID blockId;
    private String blockName;
    private String unitNumber;
    private UnitType type;
    private Integer floorNumber;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal sizeSqFt;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private OccupancyStatus occupancyStatus;
    private Boolean furnished;
    private String waterMeterNumber;
    private String electricityMeterNumber;
    private String description;
    private String status;
}
