package com.rentflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    private String model;
    private Integer maxTokens;
    private Double temperature;
}
