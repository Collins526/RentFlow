package com.rentflow.service.impl;

import com.rentflow.dto.request.OrganizationUpdateRequest;
import com.rentflow.dto.response.OrganizationResponse;
import com.rentflow.entity.Organization;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.OrganizationMapper;
import com.rentflow.repository.OrganizationRepository;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationResponse> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(OrganizationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        return OrganizationMapper.toDto(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getMyOrganization() {
        UUID organizationId = getCurrentUserOrganizationId();
        return getOrganizationById(organizationId);
    }

    @Override
    @Transactional
    public OrganizationResponse updateMyOrganization(OrganizationUpdateRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        organization.setName(request.getName());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddress(request.getAddress());
        organization.setLogoUrl(request.getLogoUrl());

        Organization updatedOrganization = organizationRepository.save(organization);
        return OrganizationMapper.toDto(updatedOrganization);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganizationById(UUID id, OrganizationUpdateRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        organization.setName(request.getName());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddress(request.getAddress());
        organization.setLogoUrl(request.getLogoUrl());

        Organization updatedOrganization = organizationRepository.save(organization);
        return OrganizationMapper.toDto(updatedOrganization);
    }

    private UUID getCurrentUserOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UUID orgId = userDetails.getUser().getOrganizationId();
        
        if (orgId == null) {
            throw new ResourceNotFoundException("User does not belong to any organization");
        }
        
        return orgId;
    }
}
