package com.rentflow.service.impl;

import com.rentflow.config.AiProperties;
import com.rentflow.dto.response.AiResponse;
import com.rentflow.exception.BadRequestException;
import com.rentflow.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AiResponse generateCompletion(String prompt,
                                         String model,
                                         Integer maxTokens,
                                         Double temperature) {
        validateConfiguration();

        if (prompt == null || prompt.isBlank()) {
            throw new BadRequestException("Prompt must not be empty");
        }

        String requestUrl = aiProperties.getBaseUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model != null ? model : aiProperties.getDefaultModel());
        body.put("prompt", prompt);
        body.put("max_tokens", maxTokens != null ? maxTokens : aiProperties.getMaxTokens());
        body.put("temperature", temperature != null ? temperature : aiProperties.getTemperature());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(requestUrl, HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) {
            throw new BadRequestException("AI provider returned empty response");
        }

        String text = extractText(responseBody);
        return AiResponse.builder()
                .model(Objects.toString(body.get("model"), ""))
                .text(text)
                .success(true)
                .build();
    }

    private void validateConfiguration() {
        if (aiProperties.getBaseUrl() == null || aiProperties.getBaseUrl().isBlank()) {
            throw new IllegalStateException("AI base URL is not configured");
        }
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("AI API key is not configured");
        }
    }

    private String extractText(Map<String, Object> responseBody) {
        if (responseBody.containsKey("choices")) {
            var choices = (Iterable<?>) responseBody.get("choices");
            for (Object item : choices) {
                if (item instanceof Map<?, ?> choice) {
                    Object text = choice.get("text");
                    if (text != null) {
                        return Objects.toString(text);
                    }
                }
            }
        }

        return Objects.toString(responseBody.get("output"), "");
    }
}
