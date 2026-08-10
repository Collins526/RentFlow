package com.rentflow.dto.request;

import com.rentflow.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PaymentRequest {
    private UUID invoiceId;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID unitId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    private String reference;

    private String phoneNumber;

    private LocalDate paymentDate;
}
