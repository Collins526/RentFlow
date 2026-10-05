package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.SalesInquiryRequest;
import com.rentflow.dto.response.SalesInquiryResponse;
import com.rentflow.service.SalesInquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales-inquiries")
@RequiredArgsConstructor
public class SalesInquiryController {

    private final SalesInquiryService service;

    @PostMapping
    public ResponseEntity<ApiResponse<SalesInquiryResponse>> submit(@Valid @RequestBody SalesInquiryRequest request) {
        SalesInquiryResponse response = service.submit(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Inquiry sent to sales"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SalesInquiryResponse>>> list(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable), "Sales inquiries fetched"));
    }
}