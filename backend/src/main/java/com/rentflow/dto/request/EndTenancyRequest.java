package com.rentflow.dto.request;

import com.rentflow.entity.enums.TenancyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Closes out a tenancy and releases its unit. Kept separate from
 * {@link TenancyRequest} because ending a tenancy must not be able to
 * silently rewrite the tenant, unit or agreed rent.
 */
@Data
public class EndTenancyRequest {

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    /**
     * Terminal status to apply. Defaults to {@link TenancyStatus#PAST}; supply
     * {@link TenancyStatus#EVICTED} to record an involuntary termination.
     */
    private TenancyStatus status;
}
