package com.rentflow.service;

import com.rentflow.dto.response.DashboardSummaryResponse;

public interface DashboardService {

    /** Portfolio counters for the current user's organization. */
    DashboardSummaryResponse getSummary();
}
