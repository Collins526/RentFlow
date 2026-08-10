package com.rentflow.repository;

import com.rentflow.entity.MoveInInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface MoveInInspectionRepository extends JpaRepository<MoveInInspection, UUID> {

    java.util.Optional<MoveInInspection> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<MoveInInspection> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<MoveInInspection> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<MoveInInspection> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);

    Page<MoveInInspection> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);
}
