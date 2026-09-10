package com.rentflow.mapper;

import com.rentflow.dto.response.OrganizationResponse;
import com.rentflow.entity.Organization;

public class OrganizationMapper {

    public static OrganizationResponse toDto(Organization organization) {
        if (organization == null) {
            return null;
        }

        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .address(organization.getAddress())
                .logoUrl(organization.getLogoUrl())
                .status(organization.getStatus())
                .build();
    }
}
