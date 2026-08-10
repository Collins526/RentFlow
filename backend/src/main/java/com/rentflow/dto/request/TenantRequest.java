package com.rentflow.dto.request;

import com.rentflow.entity.enums.TenantType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantRequest {

    @NotNull(message = "Tenant type is required")
    private TenantType tenantType;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phoneNumber;

    @Size(max = 50, message = "Identification type cannot exceed 50 characters")
    private String identificationType;

    @Size(max = 100, message = "Identification number cannot exceed 100 characters")
    private String identificationNumber;

    @Size(max = 150, message = "Emergency contact name cannot exceed 150 characters")
    private String emergencyContactName;

    @Size(max = 50, message = "Emergency contact phone cannot exceed 50 characters")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;
}
