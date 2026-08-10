package com.rentflow.service.impl;

import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.dto.request.TenantRequest;
import com.rentflow.dto.response.TenantResponse;
import com.rentflow.entity.Tenant;
import com.rentflow.repository.TenantRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        
        if (tenantRepository.existsByEmailAndOrganizationId(request.getEmail(), orgId)) {
            throw new IllegalArgumentException("Tenant with email " + request.getEmail() + " already exists in this organization");
        }

        Tenant tenant = Tenant.builder()
                .organizationId(orgId)
                .tenantType(request.getTenantType())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .identificationType(request.getIdentificationType())
                .identificationNumber(request.getIdentificationNumber())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        tenant = tenantRepository.save(tenant);
        return mapToResponse(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(UUID id, TenantRequest request) {
        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        Tenant tenant = getTenantOrThrow(id, orgId);
        
        if (!tenant.getEmail().equals(request.getEmail()) && 
            tenantRepository.existsByEmailAndOrganizationId(request.getEmail(), orgId)) {
            throw new IllegalArgumentException("Tenant with email " + request.getEmail() + " already exists in this organization");
        }

        tenant.setTenantType(request.getTenantType());
        tenant.setFirstName(request.getFirstName());
        tenant.setLastName(request.getLastName());
        tenant.setCompanyName(request.getCompanyName());
        tenant.setEmail(request.getEmail());
        tenant.setPhoneNumber(request.getPhoneNumber());
        tenant.setIdentificationType(request.getIdentificationType());
        tenant.setIdentificationNumber(request.getIdentificationNumber());
        tenant.setEmergencyContactName(request.getEmergencyContactName());
        tenant.setEmergencyContactPhone(request.getEmergencyContactPhone());
        
        if (request.getStatus() != null) {
            tenant.setStatus(request.getStatus());
        }

        tenant = tenantRepository.save(tenant);
        return mapToResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        return mapToResponse(getTenantOrThrow(id, orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        return tenantRepository.findByOrganizationId(orgId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        Tenant tenant = getTenantOrThrow(id, orgId);
        // Soft delete
        tenant.setStatus("INACTIVE");
        tenantRepository.save(tenant);
    }

    private Tenant getTenantOrThrow(UUID id, UUID organizationId) {
        return tenantRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .organizationId(tenant.getOrganizationId())
                .tenantType(tenant.getTenantType())
                .firstName(tenant.getFirstName())
                .lastName(tenant.getLastName())
                .companyName(tenant.getCompanyName())
                .email(tenant.getEmail())
                .phoneNumber(tenant.getPhoneNumber())
                .identificationType(tenant.getIdentificationType())
                .identificationNumber(tenant.getIdentificationNumber())
                .emergencyContactName(tenant.getEmergencyContactName())
                .emergencyContactPhone(tenant.getEmergencyContactPhone())
                .status(tenant.getStatus())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
