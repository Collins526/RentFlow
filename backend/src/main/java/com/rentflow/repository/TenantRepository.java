package com.rentflow.repository;

import com.rentflow.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.UUID;
import java.time.Instant;


public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    Page<Tenant> findByOrganizationId(UUID organizationId, Pageable pageable);
    
    Optional<Tenant> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    long countByDeletedAtIsNull();

    @Modifying
    @Query("UPDATE Tenant t SET t.deletedAt = :deletedAt WHERE t.organizationId = :organizationId AND t.deletedAt IS NULL")
    int softDeleteByOrganizationId(@Param("organizationId") UUID organizationId, @Param("deletedAt") Instant deletedAt);

    @Query("SELECT COUNT(t) FROM Tenant t JOIN Organization o ON t.organizationId = o.id "
            + "WHERE o.deletedAt IS NULL AND t.deletedAt IS NULL")
    long countForActiveOrganizations();
}
