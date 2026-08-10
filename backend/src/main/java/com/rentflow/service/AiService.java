package com.rentflow.service;

import com.rentflow.dto.response.AiResponse;

public interface AiService {

    AiResponse generateCompletion(String prompt,
                                  String model,
                                  Integer maxTokens,
                                  Double temperature);
}
