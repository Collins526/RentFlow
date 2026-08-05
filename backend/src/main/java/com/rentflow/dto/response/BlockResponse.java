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
public class BlockResponse {
    private UUID id;
    private UUID propertyId;
    private String name;
    private String description;
    private Integer numberOfFloors;
}
