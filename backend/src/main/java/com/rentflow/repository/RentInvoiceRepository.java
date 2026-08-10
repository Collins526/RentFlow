package com.rentflow.repository;

import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface RentInvoiceRepository extends JpaRepository<RentInvoice, UUID> {

    java.util.Optional<RentInvoice> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<RentInvoice> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<RentInvoice> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, InvoiceStatus status, Pageable pageable);

    Page<RentInvoice> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<RentInvoice> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, InvoiceStatus status);

    long countByOrganizationIdAndDueDateBeforeAndStatusNotAndDeletedAtIsNull(UUID organizationId, java.time.LocalDate date, InvoiceStatus status);

    java.math.BigDecimal sumAmountByOrganizationId(UUID organizationId);

    java.math.BigDecimal sumAmountByOrganizationIdAndStatus(UUID organizationId, InvoiceStatus status);
}
