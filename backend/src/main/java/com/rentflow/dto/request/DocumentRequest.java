package com.rentflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private UUID tenancyId;
    private UUID leaseId;
    private UUID unitId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String documentType;
    private String documentUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDate uploadedDate;
}
