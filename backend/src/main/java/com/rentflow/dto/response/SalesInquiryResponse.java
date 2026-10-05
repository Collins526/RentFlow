package com.rentflow.dto.response;

import com.rentflow.entity.SalesInquiry;

import java.time.Instant;
import java.util.UUID;

public record SalesInquiryResponse(UUID id, String name, String email, String message, Instant createdAt) {
    public static SalesInquiryResponse from(SalesInquiry inquiry) {
        return new SalesInquiryResponse(
                inquiry.getId(), inquiry.getName(), inquiry.getEmail(), inquiry.getMessage(), inquiry.getCreatedAt());
    }
}