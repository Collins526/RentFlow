package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.MaintenanceRequest;
import com.rentflow.dto.response.MaintenanceRequestResponse;
import com.rentflow.service.MaintenanceRequestService;
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
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT') or hasRole('STAFF')";

    private final MaintenanceRequestService maintenanceRequestService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<MaintenanceRequestResponse>> createMaintenanceRequest(
            @Valid @RequestBody MaintenanceRequest request) {
        MaintenanceRequestResponse response = maintenanceRequestService.createMaintenanceRequest(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Maintenance request created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<MaintenanceRequestResponse>>> listMaintenanceRequests(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID tenancyId,
            @RequestParam(required = false) UUID unitId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<MaintenanceRequestResponse> page = maintenanceRequestService.listMaintenanceRequests(tenantId, tenancyId, unitId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Maintenance requests fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<MaintenanceRequestResponse>> getMaintenanceRequest(@PathVariable UUID id) {
        MaintenanceRequestResponse response = maintenanceRequestService.getMaintenanceRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Maintenance request fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenanceRequest(@PathVariable UUID id) {
        maintenanceRequestService.deleteMaintenanceRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Maintenance request deleted"));
    }
}
