package com.rentflow.service;

import com.rentflow.dto.request.DocumentRequest;
import com.rentflow.dto.response.DocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(DocumentRequest request);
    DocumentResponse uploadDocument(DocumentRequest request, MultipartFile file);
    Page<DocumentResponse> listDocuments(UUID tenantId, UUID tenancyId, UUID unitId, String documentType, Pageable pageable);
    DocumentResponse getDocumentById(UUID id);
    void deleteDocument(UUID id);
}
