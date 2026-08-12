package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.TenantRequest;
import com.rentflow.dto.response.TenantResponse;
import com.rentflow.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ORGANIZATION_ADMIN') or hasRole('PROPERTY_MANAGER')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ORGANIZATION_ADMIN') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody TenantRequest request) {
        TenantResponse tenant = tenantService.createTenant(request);
        return new ResponseEntity<>(
                ApiResponse.success(tenant, "Tenant created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<TenantResponse>>> getAllTenants(Pageable pageable) {
        Page<TenantResponse> tenants = tenantService.getAllTenants(pageable);
        return ResponseEntity.ok(ApiResponse.success(tenants, "Tenants fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        TenantResponse tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(tenant, "Tenant fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable UUID id, @Valid @RequestBody TenantRequest request) {
        TenantResponse tenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success(tenant, "Tenant updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tenant deleted successfully"));
    }
}
