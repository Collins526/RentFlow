package com.rentflow.service.impl;

import com.rentflow.dto.request.MoveInInspectionRequest;
import com.rentflow.dto.response.MoveInInspectionResponse;
import com.rentflow.entity.MoveInInspection;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.MoveInInspectionStatus;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.MoveInInspectionMapper;
import com.rentflow.repository.MoveInInspectionRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.MoveInInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoveInInspectionServiceImpl implements MoveInInspectionService {

    private final MoveInInspectionRepository moveInInspectionRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public MoveInInspectionResponse createInspection(MoveInInspectionRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        MoveInInspection inspection = MoveInInspection.builder()
                .organizationId(organizationId)
                .tenantId(tenant.getId())
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .unitId(unit != null ? unit.getId() : null)
                .inspectionDate(request.getInspectionDate())
                .inspectorName(request.getInspectorName())
                .status(request.getStatus() != null ? request.getStatus() : MoveInInspectionStatus.SCHEDULED)
                .findings(request.getFindings())
                .build();

        MoveInInspection saved = moveInInspectionRepository.save(inspection);
        return MoveInInspectionMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MoveInInspectionResponse getInspectionById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        MoveInInspection inspection = moveInInspectionRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Move-in inspection not found"));
        return MoveInInspectionMapper.toDto(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MoveInInspectionResponse> listInspections(UUID tenantId, UUID tenancyId, UUID unitId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Page<MoveInInspection> page;
        if (tenantId != null) {
            page = moveInInspectionRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (tenancyId != null) {
            page = moveInInspectionRepository.findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(organizationId, tenancyId, pageable);
        } else if (unitId != null) {
            page = moveInInspectionRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else {
            page = moveInInspectionRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(MoveInInspectionMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteInspection(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        MoveInInspection inspection = moveInInspectionRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Move-in inspection not found"));

        inspection.setDeletedAt(Instant.now());
        moveInInspectionRepository.save(inspection);
    }
}
