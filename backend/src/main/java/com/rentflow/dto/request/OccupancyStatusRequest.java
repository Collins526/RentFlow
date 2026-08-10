package com.rentflow.dto.request;

import com.rentflow.entity.enums.OccupancyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OccupancyStatusRequest {

    @NotNull(message = "Occupancy status is required")
    private OccupancyStatus occupancyStatus;
}
