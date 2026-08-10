package com.rentflow.service.impl;

import com.rentflow.dto.request.MoveOutInspectionRequest;
import com.rentflow.dto.response.MoveOutInspectionResponse;
import com.rentflow.entity.MoveOutInspection;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.MoveOutInspectionMapper;
import com.rentflow.repository.MoveOutInspectionRepository;
import com.rentflow.service.MoveOutInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoveOutInspectionServiceImpl implements MoveOutInspectionService {

    private final MoveOutInspectionRepository repository;
    private final MoveOutInspectionMapper mapper;

    @Override
    public MoveOutInspectionResponse create(MoveOutInspectionRequest request) {
        MoveOutInspection inspection = MoveOutInspection.builder()
                .organizationId(request.getOrganizationId())
                .tenantId(request.getTenantId())
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .unitId(request.getUnitId())
                .inspectionDate(request.getInspectionDate())
                .inspectorName(request.getInspectorName())
                .status(request.getStatus())
                .findings(request.getFindings())
                .build();

        return mapper.toResponse(repository.save(inspection));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoveOutInspectionResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MoveOutInspectionResponse findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Move-out inspection not found with id: " + id));
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Move-out inspection not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
