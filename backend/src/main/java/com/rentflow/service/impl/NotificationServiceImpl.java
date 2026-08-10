package com.rentflow.service.impl;

import com.rentflow.dto.request.NotificationRequest;
import com.rentflow.dto.response.NotificationResponse;
import com.rentflow.entity.Notification;
import com.rentflow.entity.enums.NotificationStatus;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.NotificationMapper;
import com.rentflow.repository.NotificationRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (request.getTenantId() != null) {
            tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        }

        if (request.getUnitId() != null) {
            unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        Notification notification = Notification.builder()
                .organizationId(organizationId)
                .tenantId(request.getTenantId())
                .tenancyId(request.getTenancyId())
                .unitId(request.getUnitId())
                .title(request.getTitle())
                .message(request.getMessage())
                .channel(request.getChannel())
                .status(request.getStatus())
                .recipientAddress(request.getRecipientAddress())
                .sentAt(request.getSentAt())
                .deliveredAt(request.getDeliveredAt())
                .readAt(request.getReadAt())
                .build();

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listNotifications(UUID tenantId, UUID tenancyId, UUID unitId, String status, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Page<Notification> page;

        if (tenantId != null) {
            page = notificationRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (tenancyId != null) {
            page = notificationRepository.findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(organizationId, tenancyId, pageable);
        } else if (unitId != null) {
            page = notificationRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else if (status != null) {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            page = notificationRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, notificationStatus, pageable);
        } else {
            page = notificationRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Notification notification = notificationRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return notificationMapper.toResponse(notification);
    }

    @Override
    public void deleteNotification(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Notification notification = notificationRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setDeletedAt(Instant.now());
        notificationRepository.save(notification);
    }
}
