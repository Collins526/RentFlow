package com.rentflow.repository;

import com.rentflow.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {

    Page<Block> findByPropertyId(UUID propertyId, Pageable pageable);

    @Query("SELECT b FROM Block b JOIN Property p ON b.propertyId = p.id " +
           "WHERE b.id = :blockId AND p.organizationId = :organizationId")
    Optional<Block> findByIdAndOrganizationId(@Param("blockId") UUID blockId,
                                              @Param("organizationId") UUID organizationId);
}
