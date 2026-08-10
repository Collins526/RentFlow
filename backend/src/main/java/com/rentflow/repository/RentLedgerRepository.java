package com.rentflow.repository;

import com.rentflow.entity.RentLedgerEntry;
import com.rentflow.entity.enums.LedgerEntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.UUID;


public interface RentLedgerRepository extends JpaRepository<RentLedgerEntry, UUID> {

    java.util.Optional<RentLedgerEntry> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<RentLedgerEntry> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<RentLedgerEntry> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<RentLedgerEntry> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<RentLedgerEntry> findByOrganizationIdAndEntryDateBetweenAndDeletedAtIsNull(UUID organizationId, LocalDate start, LocalDate end, Pageable pageable);

    Page<RentLedgerEntry> findByOrganizationIdAndTypeAndDeletedAtIsNull(UUID organizationId, LedgerEntryType type, Pageable pageable);
}
