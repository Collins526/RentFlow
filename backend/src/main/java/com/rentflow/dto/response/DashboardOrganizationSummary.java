package com.rentflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DashboardOrganizationSummary {
    private UUID id;
    private String name;
    private String email;
    private long properties;
    private long units;
    private long tenants;
    private long activeTenancies;
}