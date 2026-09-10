package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.PropertyRequest;
import com.rentflow.dto.response.PropertyResponse;
import com.rentflow.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("(hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')) and !hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse property = propertyService.createProperty(request);
        return new ResponseEntity<>(ApiResponse.success(property, "Property created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllProperties(
            Pageable pageable, @RequestParam(required = false) UUID organizationId) {
        Page<PropertyResponse> properties = propertyService.getAllProperties(pageable, organizationId);
        return ResponseEntity.ok(ApiResponse.success(properties, "Properties fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable UUID id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success(property, "Property fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("(hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')) and !hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable UUID id, @Valid @RequestBody PropertyRequest request) {
        PropertyResponse property = propertyService.updateProperty(id, request);
        return ResponseEntity.ok(ApiResponse.success(property, "Property updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("(hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')) and !hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable UUID id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Property deleted successfully"));
    }
}
