package com.rentflow.repository;

import com.rentflow.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    Page<Tenant> findByOrganizationId(UUID organizationId, Pageable pageable);
    
    Optional<Tenant> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
