package com.rentflow.dto.response;

import com.rentflow.entity.enums.PaymentMethod;
import com.rentflow.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID unitId;
    private UUID invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String reference;
    private String phoneNumber;
    private String externalReference;
    private LocalDate paymentDate;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}
