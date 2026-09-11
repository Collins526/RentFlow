package com.rentflow.service;

import com.rentflow.dto.request.MaintenanceRequest;
import com.rentflow.dto.response.MaintenanceRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MaintenanceRequestService {
    MaintenanceRequestResponse createMaintenanceRequest(MaintenanceRequest request);
    MaintenanceRequestResponse updateMaintenanceRequest(UUID id, MaintenanceRequest request);
    void cancelMaintenanceRequest(UUID id);
    Page<MaintenanceRequestResponse> listMaintenanceRequests(UUID tenantId, UUID tenancyId, UUID unitId, String status, Pageable pageable);
    MaintenanceRequestResponse getMaintenanceRequestById(UUID id);
    void deleteMaintenanceRequest(UUID id);
}
