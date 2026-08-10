package com.rentflow.repository;

import com.rentflow.entity.MaintenanceRequest;
import com.rentflow.entity.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    java.util.Optional<MaintenanceRequest> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<MaintenanceRequest> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<MaintenanceRequest> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<MaintenanceRequest> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);

    Page<MaintenanceRequest> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<MaintenanceRequest> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, MaintenanceStatus status, Pageable pageable);
}
