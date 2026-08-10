package com.rentflow.dto.request;

import com.rentflow.entity.enums.LeaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EndLeaseRequest {

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    /**
     * Terminal status to apply. Defaults to {@link LeaseStatus#EXPIRED}; supply
     * {@link LeaseStatus#TERMINATED} to record an involuntary termination.
     */
    private LeaseStatus status;
}
