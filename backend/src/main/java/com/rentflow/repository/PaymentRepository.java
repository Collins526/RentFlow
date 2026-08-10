package com.rentflow.repository;

import com.rentflow.entity.Payment;
import com.rentflow.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    java.util.Optional<Payment> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    java.util.Optional<Payment> findByExternalReferenceAndDeletedAtIsNull(String externalReference);

    Page<Payment> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Payment> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Payment> findByOrganizationIdAndInvoiceIdAndDeletedAtIsNull(UUID organizationId, UUID invoiceId, Pageable pageable);

    Page<Payment> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, PaymentStatus status, Pageable pageable);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, PaymentStatus status);

    java.math.BigDecimal sumAmountByOrganizationId(UUID organizationId);
}
