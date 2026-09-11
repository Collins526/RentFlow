package com.rentflow.mapper;

import com.rentflow.dto.response.DocumentResponse;
import com.rentflow.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document entity) {
        if (entity == null) {
            return null;
        }

        return DocumentResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .tenantId(entity.getTenantId())
                .tenancyId(entity.getTenancyId())
                .leaseId(entity.getLeaseId())
                .unitId(entity.getUnitId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .documentType(entity.getDocumentType())
                .documentUrl(entity.getDocumentUrl())
                .cloudinaryPublicId(entity.getCloudinaryPublicId())
                .cloudinaryResourceType(entity.getCloudinaryResourceType())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .uploadedDate(entity.getUploadedDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
