package com.rentflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResponse {

    private String model;
    private String text;
    private boolean success;
}
