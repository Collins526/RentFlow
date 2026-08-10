package com.rentflow.repository;

import com.rentflow.entity.Lease;
import com.rentflow.entity.enums.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    Optional<Lease> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<Lease> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Lease> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, LeaseStatus status, Pageable pageable);

    Page<Lease> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Lease> findByOrganizationIdAndTenantIdAndStatusAndDeletedAtIsNull(UUID organizationId, UUID tenantId, LeaseStatus status, Pageable pageable);

    Page<Lease> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<Lease> findByOrganizationIdAndUnitIdAndStatusAndDeletedAtIsNull(UUID organizationId, UUID unitId, LeaseStatus status, Pageable pageable);

    Optional<Lease> findByUnitIdAndStatusAndDeletedAtIsNull(UUID unitId, LeaseStatus status);
}
