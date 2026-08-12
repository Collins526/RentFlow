package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.MpesaPaymentRequest;
import com.rentflow.dto.response.MpesaPaymentResponse;
import com.rentflow.service.MpesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ACCOUNTANT') or hasRole('TENANT')";

    private final MpesaService mpesaService;

    @PostMapping("/stk-push")
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<MpesaPaymentResponse>> initiateStkPush(
            @Valid @RequestBody MpesaPaymentRequest request) {
        var result = mpesaService.initiateStkPush(
                request.getPhoneNumber(),
                request.getAmount(),
                request.getAccountReference(),
                request.getTransactionDesc(),
                request.getTenantId(),
                request.getInvoiceId(),
                request.getUnitId(),
                request.getReference());
        return new ResponseEntity<>(ApiResponse.success(result, "M-Pesa STK push initiated"), HttpStatus.OK);
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<Map<String, String>>> receiveCallback(
            @RequestBody Map<String, Object> callbackPayload) {
        mpesaService.processCallback(callbackPayload);
        Map<String, String> ack = new HashMap<>();
        ack.put("status", "received");
        ack.put("message", "M-Pesa callback processed successfully");
        return ResponseEntity.ok(ApiResponse.success(ack, "M-Pesa callback received"));
    }
}
