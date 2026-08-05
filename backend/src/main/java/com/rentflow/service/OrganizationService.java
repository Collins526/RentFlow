package com.rentflow.service;

import com.rentflow.dto.request.OrganizationUpdateRequest;
import com.rentflow.dto.response.OrganizationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationService {
    Page<OrganizationResponse> getAllOrganizations(Pageable pageable);
    OrganizationResponse getOrganizationById(UUID id);
    OrganizationResponse getMyOrganization();
    OrganizationResponse updateMyOrganization(OrganizationUpdateRequest request);
}
