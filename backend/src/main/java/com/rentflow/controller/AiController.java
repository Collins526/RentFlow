package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.AiRequest;
import com.rentflow.dto.response.AiResponse;
import com.rentflow.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private static final String AI_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";

    private final AiService aiService;

    @PostMapping("/completions")
    @PreAuthorize(AI_ROLES)
    public ResponseEntity<ApiResponse<AiResponse>> generateCompletion(
            @Valid @RequestBody AiRequest request) {
        AiResponse result = aiService.generateCompletion(
                request.getPrompt(),
                request.getModel(),
                request.getMaxTokens(),
                request.getTemperature());
        return ResponseEntity.ok(ApiResponse.success(result, "AI completion generated successfully"));
    }
}
