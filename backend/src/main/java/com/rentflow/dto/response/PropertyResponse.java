package com.rentflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String type;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String description;
    private String status;
    private long units;
    private Integer numberOfUnits;
    private Integer numberOfFloors;
}
