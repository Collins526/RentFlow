package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.AdvanceAllocationRequest;
import com.rentflow.dto.request.AdvancePaymentRequest;
import com.rentflow.dto.response.AdvancePaymentResponse;
import com.rentflow.service.AdvancePaymentService;
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
@RequestMapping("/api/v1/advances")
@RequiredArgsConstructor
public class AdvancePaymentController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final AdvancePaymentService advancePaymentService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<AdvancePaymentResponse>> receiveAdvance(@Valid @RequestBody AdvancePaymentRequest request) {
        AdvancePaymentResponse resp = advancePaymentService.receiveAdvance(request);
        return new ResponseEntity<>(ApiResponse.success(resp, "Advance received"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/allocate")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<AdvancePaymentResponse>> allocateAdvance(
            @PathVariable UUID id,
            @Valid @RequestBody AdvanceAllocationRequest request) {
        AdvancePaymentResponse resp = advancePaymentService.allocateAdvance(id, request.getInvoiceId(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(resp, "Advance allocated to invoice"));
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<AdvancePaymentResponse>>> listAdvances(@RequestParam(required = false) UUID tenantId, Pageable pageable) {
        Page<AdvancePaymentResponse> page = advancePaymentService.listAdvances(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Advances fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<AdvancePaymentResponse>> getAdvance(@PathVariable UUID id) {
        AdvancePaymentResponse resp = advancePaymentService.getAdvanceById(id);
        return ResponseEntity.ok(ApiResponse.success(resp, "Advance fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteAdvance(@PathVariable UUID id) {
        advancePaymentService.deleteAdvance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Advance deleted"));
    }
}
