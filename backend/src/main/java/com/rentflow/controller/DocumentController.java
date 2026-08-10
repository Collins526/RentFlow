package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.DocumentRequest;
import com.rentflow.dto.response.DocumentResponse;
import com.rentflow.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT') or hasRole('STAFF')";

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @Valid @RequestBody DocumentRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Document created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> listDocuments(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID tenancyId,
            @RequestParam(required = false) UUID unitId,
            @RequestParam(required = false) String documentType,
            Pageable pageable) {
        Page<DocumentResponse> page = documentService.listDocuments(tenantId, tenancyId, unitId, documentType, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Documents fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable UUID id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Document fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted"));
    }
}
