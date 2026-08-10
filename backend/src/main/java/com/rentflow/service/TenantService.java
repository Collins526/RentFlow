package com.rentflow.service;

import com.rentflow.dto.request.TenantRequest;
import com.rentflow.dto.response.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {
    
    TenantResponse createTenant(TenantRequest request);
    
    TenantResponse updateTenant(UUID id, TenantRequest request);
    
    TenantResponse getTenantById(UUID id);
    
    Page<TenantResponse> getAllTenants(Pageable pageable);
    
    void deleteTenant(UUID id);
}
