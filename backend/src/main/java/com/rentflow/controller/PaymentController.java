package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.PaymentRequest;
import com.rentflow.dto.response.PaymentResponse;
import com.rentflow.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return new ResponseEntity<>(ApiResponse.success(payment, "Payment recorded"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID invoiceId,
            Pageable pageable) {
        Page<PaymentResponse> page = paymentService.getPayments(tenantId, invoiceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Payments fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        PaymentResponse p = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(p, "Payment fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment deleted"));
    }
}
