package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.OrganizationUpdateRequest;
import com.rentflow.dto.response.OrganizationResponse;
import com.rentflow.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> getAllOrganizations(Pageable pageable) {
        Page<OrganizationResponse> organizations = organizationService.getAllOrganizations(pageable);
        return ResponseEntity.ok(ApiResponse.success(organizations, "Organizations fetched successfully"));
    }

    @GetMapping("/my-organization")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getMyOrganization() {
        OrganizationResponse organization = organizationService.getMyOrganization();
        return ResponseEntity.ok(ApiResponse.success(organization, "Organization fetched successfully"));
    }

    @PutMapping("/my-organization")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateMyOrganization(
            @Valid @RequestBody OrganizationUpdateRequest request) {
        OrganizationResponse updatedOrganization = organizationService.updateMyOrganization(request);
        return ResponseEntity.ok(ApiResponse.success(updatedOrganization, "Organization updated successfully"));
    }
}
