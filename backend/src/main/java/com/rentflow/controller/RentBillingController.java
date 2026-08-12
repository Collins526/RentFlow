package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.RentInvoiceRequest;
import com.rentflow.dto.response.RentInvoiceResponse;
import com.rentflow.entity.enums.InvoiceStatus;
import com.rentflow.service.RentBillingService;
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
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class RentBillingController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT') or hasRole('TENANT')";

    private final RentBillingService rentBillingService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RentInvoiceResponse>> createInvoice(@Valid @RequestBody RentInvoiceRequest request) {
        RentInvoiceResponse invoice = rentBillingService.createInvoice(request);
        return new ResponseEntity<>(ApiResponse.success(invoice, "Invoice created"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RentInvoiceResponse>> issueInvoice(@PathVariable UUID id) {
        RentInvoiceResponse invoice = rentBillingService.issueInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice issued"));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RentInvoiceResponse>> markPaid(@PathVariable UUID id) {
        RentInvoiceResponse invoice = rentBillingService.markPaid(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice marked paid"));
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<RentInvoiceResponse>>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID unitId,
            Pageable pageable) {
        Page<RentInvoiceResponse> page = rentBillingService.getInvoices(status, tenantId, unitId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Invoices fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<RentInvoiceResponse>> getInvoice(@PathVariable UUID id) {
        RentInvoiceResponse invoice = rentBillingService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable UUID id) {
        rentBillingService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted"));
    }
}
