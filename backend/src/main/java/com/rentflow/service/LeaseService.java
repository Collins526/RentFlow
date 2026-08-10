package com.rentflow.service;

import com.rentflow.dto.request.EndLeaseRequest;
import com.rentflow.dto.request.LeaseRequest;
import com.rentflow.dto.response.LeaseResponse;
import com.rentflow.entity.enums.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LeaseService {

    LeaseResponse createLease(LeaseRequest request);

    LeaseResponse updateLease(UUID id, LeaseRequest request);

    LeaseResponse getLeaseById(UUID id);

    Page<LeaseResponse> getLeases(LeaseStatus status, UUID tenantId, UUID unitId, Pageable pageable);

    LeaseResponse endLease(UUID id, EndLeaseRequest request);

    void deleteLease(UUID id);
}
