package com.rentflow.service;

import com.rentflow.dto.request.MoveInInspectionRequest;
import com.rentflow.dto.response.MoveInInspectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MoveInInspectionService {

    MoveInInspectionResponse createInspection(MoveInInspectionRequest request);

    MoveInInspectionResponse getInspectionById(UUID id);

    Page<MoveInInspectionResponse> listInspections(UUID tenantId, UUID tenancyId, UUID unitId, Pageable pageable);

    void deleteInspection(UUID id);
}
