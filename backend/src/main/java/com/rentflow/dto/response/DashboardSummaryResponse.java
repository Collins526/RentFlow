package com.rentflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** Counters and analysis for either one organization or the whole platform. */
@Data
@Builder
public class DashboardSummaryResponse {

    private long totalOrganizations;
    private List<DashboardOrganizationSummary> organizations;
    private long totalProperties;
    private long totalBlocks;

    private long totalUnits;
    private long occupiedUnits;
    private long vacantUnits;
    private long reservedUnits;
    private long unitsUnderMaintenance;

    /** Occupied units as a percentage of all units, rounded to one decimal place. */
    private BigDecimal occupancyRate;

    private long totalTenants;
    private long activeTenancies;
    private long upcomingTenancies;

    /** Rent actually contracted through currently active tenancies. */
    private BigDecimal contractedMonthlyRent;

    /** Rent the portfolio would earn at full occupancy, from the units' own rates. */
    private BigDecimal potentialMonthlyRent;
}
