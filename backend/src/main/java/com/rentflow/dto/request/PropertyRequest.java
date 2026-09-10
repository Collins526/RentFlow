package com.rentflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PropertyRequest {
    @NotBlank(message = "Property name is required")
    @Size(max = 150, message = "Property name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Property type is required")
    @Size(max = 50, message = "Property type cannot exceed 50 characters")
    private String type;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zipCode;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    private String description;
    
    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;

    @Min(value = 0, message = "Number of units cannot be negative")
    private Integer numberOfUnits = 0;

    @Min(value = 0, message = "Number of floors cannot be negative")
    private Integer numberOfFloors = 0;
}
