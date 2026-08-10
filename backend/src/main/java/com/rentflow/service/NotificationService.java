package com.rentflow.service;

import com.rentflow.dto.request.NotificationRequest;
import com.rentflow.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse createNotification(NotificationRequest request);
    Page<NotificationResponse> listNotifications(UUID tenantId, UUID tenancyId, UUID unitId, String status, Pageable pageable);
    NotificationResponse getNotificationById(UUID id);
    void deleteNotification(UUID id);
}
