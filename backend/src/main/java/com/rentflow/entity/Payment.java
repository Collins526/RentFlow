package com.rentflow.entity;

import com.rentflow.common.BaseEntity;
import com.rentflow.entity.enums.PaymentMethod;
import com.rentflow.entity.enums.PaymentStatus;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "reference", length = 500)
    private String reference;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "paid_at")
    private Instant paidAt;
}
