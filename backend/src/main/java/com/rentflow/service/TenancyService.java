package com.rentflow.service;

import com.rentflow.dto.request.EndTenancyRequest;
import com.rentflow.dto.request.TenancyRequest;
import com.rentflow.dto.response.TenancyResponse;
import com.rentflow.entity.enums.TenancyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenancyService {

    TenancyResponse createTenancy(TenancyRequest request);

    TenancyResponse updateTenancy(UUID id, TenancyRequest request);

    TenancyResponse getTenancyById(UUID id);

    /**
     * Lists tenancies in the caller's organization. Every filter is optional; passing
     * {@code null} for all three returns the whole organization.
     */
    Page<TenancyResponse> getTenancies(TenancyStatus status, UUID tenantId, UUID unitId, Pageable pageable);

    /** Closes a tenancy on a given date and releases its unit. */
    TenancyResponse endTenancy(UUID id, EndTenancyRequest request);

    void deleteTenancy(UUID id);
}
