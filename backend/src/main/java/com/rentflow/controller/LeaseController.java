package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.EndLeaseRequest;
import com.rentflow.dto.request.LeaseRequest;
import com.rentflow.dto.response.LeaseResponse;
import com.rentflow.entity.enums.LeaseStatus;
import com.rentflow.service.LeaseService;
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
@RequestMapping("/api/v1/leases")
@RequiredArgsConstructor
public class LeaseController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final LeaseService leaseService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<LeaseResponse>> createLease(@Valid @RequestBody LeaseRequest request) {
        LeaseResponse lease = leaseService.createLease(request);
        return new ResponseEntity<>(ApiResponse.success(lease, "Lease created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<LeaseResponse>>> getLeases(
            @RequestParam(required = false) LeaseStatus status,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID unitId,
            Pageable pageable) {
        Page<LeaseResponse> leases = leaseService.getLeases(status, tenantId, unitId, pageable);
        return ResponseEntity.ok(ApiResponse.success(leases, "Leases fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<LeaseResponse>> getLeaseById(@PathVariable UUID id) {
        LeaseResponse lease = leaseService.getLeaseById(id);
        return ResponseEntity.ok(ApiResponse.success(lease, "Lease fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<LeaseResponse>> updateLease(
            @PathVariable UUID id, @Valid @RequestBody LeaseRequest request) {
        LeaseResponse lease = leaseService.updateLease(id, request);
        return ResponseEntity.ok(ApiResponse.success(lease, "Lease updated successfully"));
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<LeaseResponse>> endLease(
            @PathVariable UUID id, @Valid @RequestBody EndLeaseRequest request) {
        LeaseResponse lease = leaseService.endLease(id, request);
        return ResponseEntity.ok(ApiResponse.success(lease, "Lease ended successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteLease(@PathVariable UUID id) {
        leaseService.deleteLease(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lease deleted successfully"));
    }
}
