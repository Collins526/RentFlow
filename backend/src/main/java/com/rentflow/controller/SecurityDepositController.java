package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.SecurityDepositRefundRequest;
import com.rentflow.dto.request.SecurityDepositRequest;
import com.rentflow.dto.response.SecurityDepositResponse;
import com.rentflow.service.SecurityDepositService;
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
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
public class SecurityDepositController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final SecurityDepositService securityDepositService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> createDeposit(
            @Valid @RequestBody SecurityDepositRequest request) {
        SecurityDepositResponse response = securityDepositService.createDeposit(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Security deposit created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<SecurityDepositResponse>>> listDeposits(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID tenancyId,
            Pageable pageable) {
        Page<SecurityDepositResponse> page = securityDepositService.listDeposits(tenantId, tenancyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Security deposits fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> getDeposit(@PathVariable UUID id) {
        SecurityDepositResponse response = securityDepositService.getDepositById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Security deposit fetched"));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> refundDeposit(
            @PathVariable UUID id,
            @Valid @RequestBody SecurityDepositRefundRequest request) {
        SecurityDepositResponse response = securityDepositService.refundDeposit(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Security deposit refunded"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteDeposit(@PathVariable UUID id) {
        securityDepositService.deleteDeposit(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Security deposit deleted"));
    }
}
