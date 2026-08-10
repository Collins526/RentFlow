package com.rentflow.dto.response;

import com.rentflow.entity.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RentInvoiceResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID tenantId;
    private String tenantName;
    private UUID unitId;
    private String unitNumber;
    private UUID propertyId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;
    private BigDecimal amount;
    private InvoiceStatus status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
