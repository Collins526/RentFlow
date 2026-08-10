package com.rentflow.repository;

import com.rentflow.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Page<Property> findByOrganizationId(UUID organizationId, Pageable pageable);
    Optional<Property> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
