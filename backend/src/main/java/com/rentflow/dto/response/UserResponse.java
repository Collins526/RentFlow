package com.rentflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UUID organizationId;
    private UUID tenantId;
    private UUID unitId;
    private List<String> roles;
}
