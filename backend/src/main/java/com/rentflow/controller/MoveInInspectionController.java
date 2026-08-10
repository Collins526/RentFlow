package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.MoveInInspectionRequest;
import com.rentflow.dto.response.MoveInInspectionResponse;
import com.rentflow.service.MoveInInspectionService;
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
@RequestMapping("/api/v1/move-in-inspections")
@RequiredArgsConstructor
public class MoveInInspectionController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final MoveInInspectionService moveInInspectionService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<MoveInInspectionResponse>> createInspection(
            @Valid @RequestBody MoveInInspectionRequest request) {
        MoveInInspectionResponse response = moveInInspectionService.createInspection(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Move-in inspection created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<MoveInInspectionResponse>>> listInspections(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID tenancyId,
            @RequestParam(required = false) UUID unitId,
            Pageable pageable) {
        Page<MoveInInspectionResponse> page = moveInInspectionService.listInspections(tenantId, tenancyId, unitId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Move-in inspections fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<MoveInInspectionResponse>> getInspection(@PathVariable UUID id) {
        MoveInInspectionResponse response = moveInInspectionService.getInspectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Move-in inspection fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteInspection(@PathVariable UUID id) {
        moveInInspectionService.deleteInspection(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Move-in inspection deleted"));
    }
}
