package com.rentflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MpesaPaymentRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID invoiceId;
    private UUID unitId;
    private String reference;

    private String accountReference;
    private String transactionDesc;
}
