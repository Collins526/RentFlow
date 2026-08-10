package com.rentflow.repository;

import com.rentflow.entity.SecurityDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, UUID> {

    java.util.Optional<SecurityDeposit> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<SecurityDeposit> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<SecurityDeposit> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<SecurityDeposit> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);
}
