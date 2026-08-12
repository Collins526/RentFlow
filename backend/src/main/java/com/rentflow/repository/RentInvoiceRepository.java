package com.rentflow.repository;

import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query("select coalesce(sum(r.amount), 0) from RentInvoice r where r.organizationId = :organizationId and r.deletedAt is null")
    BigDecimal sumAmountByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("select coalesce(sum(r.amount), 0) from RentInvoice r where r.organizationId = :organizationId and r.status = :status and r.deletedAt is null")
    BigDecimal sumAmountByOrganizationIdAndStatus(@Param("organizationId") UUID organizationId, @Param("status") InvoiceStatus status);
}
