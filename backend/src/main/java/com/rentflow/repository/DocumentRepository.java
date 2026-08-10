package com.rentflow.repository;

import com.rentflow.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface DocumentRepository extends JpaRepository<Document, UUID> {

    java.util.Optional<Document> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<Document> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Document> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Document> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);

    Page<Document> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<Document> findByOrganizationIdAndDocumentTypeAndDeletedAtIsNull(UUID organizationId, String documentType, Pageable pageable);
}
