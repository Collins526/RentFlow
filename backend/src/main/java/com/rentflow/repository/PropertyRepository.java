package com.rentflow.repository;

import com.rentflow.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;


import java.util.Optional;
import java.util.UUID;
import java.time.Instant;


public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Page<Property> findByOrganizationId(UUID organizationId, Pageable pageable);
    Page<Property> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);
    Optional<Property> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.organizationId = :organizationId AND p.deletedAt IS NULL")
    long countActiveByOrganizationId(@Param("organizationId") UUID organizationId);

    long countByDeletedAtIsNull();

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Property p SET p.deletedAt = :deletedAt WHERE p.organizationId = :organizationId AND p.deletedAt IS NULL")
    int softDeleteByOrganizationId(@Param("organizationId") UUID organizationId, @Param("deletedAt") Instant deletedAt);
}
