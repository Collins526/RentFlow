package com.rentflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private String baseUrl;
    private String apiKey;
    private String defaultModel = "gpt-4o-mini";
    private Integer maxTokens = 512;
    private Double temperature = 0.7;
}
