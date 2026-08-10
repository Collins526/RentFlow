package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.BulkUnitRequest;
import com.rentflow.dto.request.OccupancyStatusRequest;
import com.rentflow.dto.request.UnitRequest;
import com.rentflow.dto.response.UnitResponse;
import com.rentflow.dto.response.UnitSummaryResponse;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UnitController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final UnitService unitService;

    @PostMapping("/api/v1/properties/{propertyId}/units")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(
            @PathVariable UUID propertyId, @Valid @RequestBody UnitRequest request) {
        UnitResponse unit = unitService.createUnit(propertyId, request);
        return new ResponseEntity<>(ApiResponse.success(unit, "Unit created successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/properties/{propertyId}/units/bulk")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<List<UnitResponse>>> bulkCreateUnits(
            @PathVariable UUID propertyId, @Valid @RequestBody BulkUnitRequest request) {
        List<UnitResponse> units = unitService.bulkCreateUnits(propertyId, request);
        return new ResponseEntity<>(
                ApiResponse.success(units, units.size() + " units created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/properties/{propertyId}/units")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<UnitResponse>>> getUnitsByProperty(
            @PathVariable UUID propertyId,
            @RequestParam(required = false) OccupancyStatus occupancyStatus,
            Pageable pageable) {
        Page<UnitResponse> units = unitService.getUnitsByPropertyId(propertyId, occupancyStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success(units, "Units fetched successfully"));
    }

    @GetMapping("/api/v1/properties/{propertyId}/units/summary")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<UnitSummaryResponse>> getPropertyUnitSummary(@PathVariable UUID propertyId) {
        UnitSummaryResponse summary = unitService.getPropertyUnitSummary(propertyId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Unit summary fetched successfully"));
    }

    @GetMapping("/api/v1/blocks/{blockId}/units")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<UnitResponse>>> getUnitsByBlock(
            @PathVariable UUID blockId, Pageable pageable) {
        Page<UnitResponse> units = unitService.getUnitsByBlockId(blockId, pageable);
        return ResponseEntity.ok(ApiResponse.success(units, "Units fetched successfully"));
    }

    @GetMapping("/api/v1/units/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable UUID id) {
        UnitResponse unit = unitService.getUnitById(id);
        return ResponseEntity.ok(ApiResponse.success(unit, "Unit fetched successfully"));
    }

    @PutMapping("/api/v1/units/{id}")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(
            @PathVariable UUID id, @Valid @RequestBody UnitRequest request) {
        UnitResponse unit = unitService.updateUnit(id, request);
        return ResponseEntity.ok(ApiResponse.success(unit, "Unit updated successfully"));
    }

    @PatchMapping("/api/v1/units/{id}/occupancy")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<UnitResponse>> updateOccupancyStatus(
            @PathVariable UUID id, @Valid @RequestBody OccupancyStatusRequest request) {
        UnitResponse unit = unitService.updateOccupancyStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(unit, "Unit occupancy updated successfully"));
    }

    @DeleteMapping("/api/v1/units/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable UUID id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Unit deleted successfully"));
    }
}
