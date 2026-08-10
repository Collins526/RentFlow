package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.LedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rent_ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentLedgerEntry extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenancy_id")
    private UUID tenancyId;

    @Column(name = "lease_id")
    private UUID leaseId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LedgerEntryType type;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;
}
