package com.rentflow.service.impl;

import com.rentflow.dto.request.MaintenanceRequest;
import com.rentflow.dto.response.MaintenanceRequestResponse;
import com.rentflow.entity.enums.MaintenanceStatus;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.MaintenanceRequestMapper;
import com.rentflow.repository.MaintenanceRequestRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.MaintenanceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final MaintenanceRequestMapper maintenanceRequestMapper;

    @Override
    public MaintenanceRequestResponse createMaintenanceRequest(MaintenanceRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            UUID currentUnitId = SecurityUtils.getCurrentUser().getUser().getUnitId();

            if (currentTenantId == null || !currentTenantId.equals(request.getTenantId())) {
                throw new UnauthorizedException("Tenant can only create maintenance requests for their own tenancy");
            }
            if (request.getUnitId() != null && currentUnitId != null && !currentUnitId.equals(request.getUnitId())) {
                throw new UnauthorizedException("Tenant can only create maintenance requests for their assigned unit");
            }
        }

        tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        if (request.getUnitId() != null) {
            unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        com.rentflow.entity.MaintenanceRequest entity = com.rentflow.entity.MaintenanceRequest.builder()
                .organizationId(organizationId)
                .tenantId(request.getTenantId())
                .unitId(request.getUnitId())
                .tenancyId(request.getTenancyId())
                .title(request.getTitle())
                .description(request.getDescription())
                .attachmentData(request.getAttachmentData())
                .attachmentName(request.getAttachmentName())
                .attachmentType(request.getAttachmentType())
                .attachmentSize(request.getAttachmentSize())
                .priority(request.getPriority())
                .status(request.getStatus())
                .requestedDate(request.getRequestedDate())
                .scheduledDate(request.getScheduledDate())
                .completedDate(request.getCompletedDate())
                .assignedTo(request.getAssignedTo())
                .build();

        return maintenanceRequestMapper.toResponse(maintenanceRequestRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRequestResponse> listMaintenanceRequests(UUID tenantId, UUID tenancyId, UUID unitId, String status, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null) {
                throw new UnauthorizedException("Tenant is not linked to a tenant record");
            }
            if (tenantId != null && !currentTenantId.equals(tenantId)) {
                throw new UnauthorizedException("Tenant can only view their own maintenance requests");
            }
            tenantId = currentTenantId;
            tenancyId = null;
            unitId = null;
        }

        Page<com.rentflow.entity.MaintenanceRequest> page;

        if (tenantId != null) {
            page = maintenanceRequestRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (tenancyId != null) {
            page = maintenanceRequestRepository.findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(organizationId, tenancyId, pageable);
        } else if (unitId != null) {
            page = maintenanceRequestRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else if (status != null) {
            MaintenanceStatus maintenanceStatus = MaintenanceStatus.valueOf(status.toUpperCase());
            page = maintenanceRequestRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, maintenanceStatus, pageable);
        } else {
            page = maintenanceRequestRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(maintenanceRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRequestResponse getMaintenanceRequestById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        com.rentflow.entity.MaintenanceRequest entity = maintenanceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found"));

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null || !currentTenantId.equals(entity.getTenantId())) {
                throw new UnauthorizedException("Tenant can only access their own maintenance requests");
            }
        }

        return maintenanceRequestMapper.toResponse(entity);
    }

    @Override
    public void deleteMaintenanceRequest(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        com.rentflow.entity.MaintenanceRequest entity = maintenanceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found"));

        entity.setDeletedAt(Instant.now());
        maintenanceRequestRepository.save(entity);
    }
}
