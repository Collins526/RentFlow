package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.TenancyStatus;
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
@Table(name = "tenancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenancy extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TenancyStatus status;

    @Column(name = "rent_amount", precision = 14, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "security_deposit_amount", precision = 14, scale = 2)
    private BigDecimal securityDepositAmount;
}
