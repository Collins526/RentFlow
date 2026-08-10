package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.UnitType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit extends BaseEntity {

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    /** Optional: units may sit directly under a property when it has no blocks. */
    @Column(name = "block_id")
    private UUID blockId;

    @Column(name = "unit_number", nullable = false, length = 50)
    private String unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UnitType type;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer bedrooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer bathrooms = 0;

    @Column(name = "size_sq_ft", precision = 10, scale = 2)
    private BigDecimal sizeSqFt;

    @Column(name = "rent_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "deposit_amount", precision = 14, scale = 2)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_status", nullable = false, length = 30)
    @Builder.Default
    private OccupancyStatus occupancyStatus = OccupancyStatus.VACANT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean furnished = false;

    @Column(name = "water_meter_number", length = 50)
    private String waterMeterNumber;

    @Column(name = "electricity_meter_number", length = 50)
    private String electricityMeterNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";
}
