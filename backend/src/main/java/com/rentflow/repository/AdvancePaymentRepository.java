package com.rentflow.repository;

import com.rentflow.entity.AdvancePayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface AdvancePaymentRepository extends JpaRepository<AdvancePayment, UUID> {
    java.util.Optional<AdvancePayment> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);
    Page<AdvancePayment> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);
    Page<AdvancePayment> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);
}
