package com.rentflow.service;

import com.rentflow.dto.request.PropertyRequest;
import com.rentflow.dto.response.PropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PropertyService {
    PropertyResponse createProperty(PropertyRequest request);
    Page<PropertyResponse> getAllProperties(Pageable pageable);
    Page<PropertyResponse> getAllProperties(Pageable pageable, UUID organizationId);
    PropertyResponse getPropertyById(UUID id);
    PropertyResponse updateProperty(UUID id, PropertyRequest request);
    void deleteProperty(UUID id);
}
