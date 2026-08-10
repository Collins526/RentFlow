package com.rentflow.entity.enums;

/**
 * Physical occupancy state of a unit.
 * <p>
 * {@link #OCCUPIED} is owned exclusively by the Tenancy module and is never
 * reachable through a manual occupancy update.
 */
public enum OccupancyStatus {
    VACANT,
    RESERVED,
    OCCUPIED,
    UNDER_MAINTENANCE
}
