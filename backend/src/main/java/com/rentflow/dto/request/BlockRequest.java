package com.rentflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlockRequest {
    @NotBlank(message = "Block name is required")
    @Size(max = 100, message = "Block name cannot exceed 100 characters")
    private String name;

    private String description;

    @Min(value = 1, message = "Number of floors must be at least 1")
    private Integer numberOfFloors = 1;
}
