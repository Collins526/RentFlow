package com.rentflow.dto.response;

import com.rentflow.entity.enums.TenancyStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TenancyResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private UUID unitId;
    private String unitNumber;
    private UUID propertyId;
    private String tenantLoginPassword;
    private LocalDate startDate;
    private LocalDate endDate;
    private TenancyStatus status;
    private BigDecimal rentAmount;
    private BigDecimal securityDepositAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
