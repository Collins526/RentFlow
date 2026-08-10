package com.rentflow.service.impl;

import com.rentflow.dto.request.DocumentRequest;
import com.rentflow.dto.response.DocumentResponse;
import com.rentflow.entity.Document;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.DocumentMapper;
import com.rentflow.repository.DocumentRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final DocumentMapper documentMapper;

    @Override
    public DocumentResponse createDocument(DocumentRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        if (request.getUnitId() != null) {
            unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        Document document = Document.builder()
                .organizationId(organizationId)
                .tenantId(request.getTenantId())
                .unitId(request.getUnitId())
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .documentType(request.getDocumentType())
                .documentUrl(request.getDocumentUrl())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .uploadedDate(request.getUploadedDate())
                .build();

        return documentMapper.toResponse(documentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> listDocuments(UUID tenantId, UUID tenancyId, UUID unitId, String documentType, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Page<Document> page;

        if (tenantId != null) {
            page = documentRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (tenancyId != null) {
            page = documentRepository.findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(organizationId, tenancyId, pageable);
        } else if (unitId != null) {
            page = documentRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else if (documentType != null) {
            page = documentRepository.findByOrganizationIdAndDocumentTypeAndDeletedAtIsNull(organizationId, documentType, pageable);
        } else {
            page = documentRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Document document = documentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return documentMapper.toResponse(document);
    }

    @Override
    public void deleteDocument(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Document document = documentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        document.setDeletedAt(Instant.now());
        documentRepository.save(document);
    }
}
