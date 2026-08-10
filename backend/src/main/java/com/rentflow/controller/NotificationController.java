package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.NotificationRequest;
import com.rentflow.dto.response.NotificationResponse;
import com.rentflow.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final String MANAGE_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT')";
    private static final String READ_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('ACCOUNTANT') or hasRole('STAFF')";

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize(MANAGE_ROLES)
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Notification created"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> listNotifications(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID tenancyId,
            @RequestParam(required = false) UUID unitId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<NotificationResponse> page = notificationService.listNotifications(tenantId, tenancyId, unitId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Notifications fetched"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ_ROLES)
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable UUID id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification fetched"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }
}
