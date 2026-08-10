package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.RentLedgerRequest;
import com.rentflow.dto.response.RentLedgerResponse;
import com.rentflow.service.RentLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class RentLedgerController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final RentLedgerService rentLedgerService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RentLedgerResponse>> createEntry(@Valid @RequestBody RentLedgerRequest request) {
        RentLedgerResponse entry = rentLedgerService.createEntry(request);
        return new ResponseEntity<>(ApiResponse.success(entry, "Ledger entry created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<RentLedgerResponse>>> getEntries(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {

        Page<RentLedgerResponse> page = rentLedgerService.getEntries(tenantId, unitId, start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Ledger entries fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<RentLedgerResponse>> getEntry(@PathVariable UUID id) {
        RentLedgerResponse entry = rentLedgerService.getEntryById(id);
        return ResponseEntity.ok(ApiResponse.success(entry, "Ledger entry fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(@PathVariable UUID id) {
        rentLedgerService.deleteEntry(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Ledger entry deleted"));
    }
}
