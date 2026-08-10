package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.EndTenancyRequest;
import com.rentflow.dto.request.TenancyRequest;
import com.rentflow.dto.response.TenancyResponse;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.service.TenancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenancies")
@RequiredArgsConstructor
public class TenancyController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final TenancyService tenancyService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<TenancyResponse>> createTenancy(
            @Valid @RequestBody TenancyRequest request) {
        TenancyResponse tenancy = tenancyService.createTenancy(request);
        return new ResponseEntity<>(
                ApiResponse.success(tenancy, "Tenancy created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<TenancyResponse>>> getTenancies(
            @RequestParam(required = false) TenancyStatus status,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID unitId,
            Pageable pageable) {
        Page<TenancyResponse> tenancies = tenancyService.getTenancies(status, tenantId, unitId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tenancies, "Tenancies fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<TenancyResponse>> getTenancyById(@PathVariable UUID id) {
        TenancyResponse tenancy = tenancyService.getTenancyById(id);
        return ResponseEntity.ok(ApiResponse.success(tenancy, "Tenancy fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<TenancyResponse>> updateTenancy(
            @PathVariable UUID id, @Valid @RequestBody TenancyRequest request) {
        TenancyResponse tenancy = tenancyService.updateTenancy(id, request);
        return ResponseEntity.ok(ApiResponse.success(tenancy, "Tenancy updated successfully"));
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<TenancyResponse>> endTenancy(
            @PathVariable UUID id, @Valid @RequestBody EndTenancyRequest request) {
        TenancyResponse tenancy = tenancyService.endTenancy(id, request);
        return ResponseEntity.ok(ApiResponse.success(tenancy, "Tenancy ended successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteTenancy(@PathVariable UUID id) {
        tenancyService.deleteTenancy(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tenancy deleted successfully"));
    }
}
