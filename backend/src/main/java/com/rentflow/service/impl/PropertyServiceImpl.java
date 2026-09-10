package com.rentflow.service.impl;

import com.rentflow.dto.request.PropertyRequest;
import com.rentflow.dto.response.PropertyResponse;
import com.rentflow.entity.Property;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.PropertyMapper;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.PropertyService;
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
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();
        Property property = PropertyMapper.toEntity(request, organizationId);
        Property savedProperty = propertyRepository.save(property);
        return toResponse(savedProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAllProperties(Pageable pageable) {
        return getAllProperties(pageable, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAllProperties(Pageable pageable, UUID requestedOrganizationId) {
        UUID organizationId = SecurityUtils.isPlatformAdmin() && requestedOrganizationId != null
                ? requestedOrganizationId
                : getCurrentUserOrganizationId();
        return propertyRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        Property property = getPropertyEntity(id);
        return toResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateProperty(UUID id, PropertyRequest request) {
        Property property = getPropertyEntity(id);

        property.setName(request.getName());
        property.setType(request.getType());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setZipCode(request.getZipCode());
        property.setCountry(request.getCountry());
        property.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            property.setStatus(request.getStatus());
        }
        if (request.getNumberOfUnits() != null) {
            property.setNumberOfUnits(request.getNumberOfUnits());
        }
        if (request.getNumberOfFloors() != null) {
            property.setNumberOfFloors(request.getNumberOfFloors());
        }

        Property updatedProperty = propertyRepository.save(property);
        return toResponse(updatedProperty);
    }

    @Override
    @Transactional
    public void deleteProperty(UUID id) {
        Property property = getPropertyEntity(id);
        propertyRepository.delete(property);
    }

    private Property getPropertyEntity(UUID id) {
        UUID organizationId = getCurrentUserOrganizationId();
        return propertyRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    private PropertyResponse toResponse(Property property) {
        PropertyResponse response = PropertyMapper.toDto(property);
        response.setUnits(unitRepository.countByPropertyIdAndDeletedAtIsNull(property.getId()));
        return response;
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
