package com.rentflow.service;

import com.rentflow.dto.request.BulkUnitRequest;
import com.rentflow.dto.request.OccupancyStatusRequest;
import com.rentflow.dto.request.UnitRequest;
import com.rentflow.dto.response.UnitResponse;
import com.rentflow.dto.response.UnitSummaryResponse;
import com.rentflow.entity.enums.OccupancyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UnitService {
    UnitResponse createUnit(UUID propertyId, UnitRequest request);
    List<UnitResponse> bulkCreateUnits(UUID propertyId, BulkUnitRequest request);
    Page<UnitResponse> getUnitsByPropertyId(UUID propertyId, OccupancyStatus occupancyStatus, Pageable pageable);
    Page<UnitResponse> getUnitsByBlockId(UUID blockId, Pageable pageable);
    UnitSummaryResponse getPropertyUnitSummary(UUID propertyId);
    UnitResponse getUnitById(UUID id);
    UnitResponse updateUnit(UUID id, UnitRequest request);
    UnitResponse updateOccupancyStatus(UUID id, OccupancyStatusRequest request);
    void setOccupiedStatusForTenancy(UUID id, boolean isOccupied);
    void deleteUnit(UUID id);
}
