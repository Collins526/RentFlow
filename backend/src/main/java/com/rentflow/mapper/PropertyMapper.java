package com.rentflow.mapper;

import com.rentflow.dto.request.PropertyRequest;
import com.rentflow.dto.response.PropertyResponse;
import com.rentflow.entity.Property;

import java.util.UUID;

public class PropertyMapper {

    public static PropertyResponse toDto(Property property) {
        if (property == null) {
            return null;
        }

        return PropertyResponse.builder()
                .id(property.getId())
                .organizationId(property.getOrganizationId())
                .name(property.getName())
                .type(property.getType())
                .address(property.getAddress())
                .city(property.getCity())
                .state(property.getState())
                .zipCode(property.getZipCode())
                .country(property.getCountry())
                .description(property.getDescription())
                .status(property.getStatus())
                .build();
    }

    public static Property toEntity(PropertyRequest request, UUID organizationId) {
        if (request == null) {
            return null;
        }

        return Property.builder()
                .organizationId(organizationId)
                .name(request.getName())
                .type(request.getType())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
    }
}
