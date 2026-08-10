package com.rentflow.repository;

import com.rentflow.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
