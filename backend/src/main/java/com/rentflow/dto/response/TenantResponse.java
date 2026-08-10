package com.rentflow.dto.response;

import com.rentflow.entity.enums.TenantType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TenantResponse {
    private UUID id;
    private UUID organizationId;
    private TenantType tenantType;
    private String firstName;
    private String lastName;
    private String companyName;
    private String email;
    private String phoneNumber;
    private String identificationType;
    private String identificationNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
