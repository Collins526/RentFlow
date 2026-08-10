package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.response.DashboardSummaryResponse;
import com.rentflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        DashboardSummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary fetched successfully"));
    }
}
