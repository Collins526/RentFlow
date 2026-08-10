package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.response.ReportSummaryResponse;
import com.rentflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getSummaryReport() {
        ReportSummaryResponse response = reportService.getSummaryReport();
        return ResponseEntity.ok(ApiResponse.success(response, "Report summary fetched"));
    }
}
