package com.rentflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UUID organizationId;
    private List<String> roles;
    private List<String> permissions;
}
