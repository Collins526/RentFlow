package com.rentflow.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SuperAdminProperties {

    private String email = "admin@rentflow.com";
    private String password = "RentFlow123!";
    private String firstName = "Super";
    private String lastName = "Admin";
}
