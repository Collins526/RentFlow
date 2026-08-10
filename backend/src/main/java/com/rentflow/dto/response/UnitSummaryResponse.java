package com.rentflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Occupancy roll-up for a property, used by the property dashboard header.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitSummaryResponse {
    private long totalUnits;
    private long vacant;
    private long reserved;
    private long occupied;
    private long underMaintenance;

    /** Sum of rent across all live units, i.e. revenue at full occupancy. */
    private BigDecimal totalPotentialRent;

    /** Sum of rent across currently occupied units. */
    private BigDecimal currentContractedRent;
}
