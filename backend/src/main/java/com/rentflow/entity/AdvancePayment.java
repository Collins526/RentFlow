package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.AdvanceStatus;
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
@Table(name = "advance_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvancePayment extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "lease_id")
    private UUID leaseId;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "remaining_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdvanceStatus status;

    @Column(name = "note", length = 2000)
    private String note;

    @Column(name = "received_date")
    private LocalDate receivedDate;
}
