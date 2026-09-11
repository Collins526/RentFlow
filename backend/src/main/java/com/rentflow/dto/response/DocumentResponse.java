package com.rentflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private UUID id;
    private UUID organizationId;
    private UUID tenantId;
    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;
    private String title;
    private String description;
    private String documentType;
    private String documentUrl;
    private String cloudinaryPublicId;
    private String cloudinaryResourceType;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDate uploadedDate;
    private Instant createdAt;
    private Instant updatedAt;
}
