package com.rentflow.service.impl;

import com.rentflow.dto.request.NotificationRequest;
import com.rentflow.dto.request.TenantAnnouncementRequest;
import com.rentflow.dto.response.NotificationResponse;
import com.rentflow.entity.Notification;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.enums.NotificationChannel;
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
import java.time.LocalDateTime;
import java.util.List;
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
    public int announceToAllTenants(TenantAnnouncementRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        List<Tenant> tenants = tenantRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId);
        if (tenants.isEmpty()) {
            return 0;
        }

        LocalDateTime sentAt = LocalDateTime.now();
        List<Notification> announcements = tenants.stream()
                .map(tenant -> Notification.builder()
                        .organizationId(organizationId)
                        .tenantId(tenant.getId())
                        .announcement(true)
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .channel(NotificationChannel.IN_APP)
                        .status(NotificationStatus.DELIVERED)
                        .sentAt(sentAt)
                        .deliveredAt(sentAt)
                        .build())
                .toList();

        notificationRepository.saveAll(announcements);
        return announcements.size();
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
    public Page<NotificationResponse> listMyUnreadInvoiceNotifications(Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID tenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant profile not found");
        }

        return notificationRepository
                .findByOrganizationIdAndTenantIdAndChannelAndInvoiceIdIsNotNullAndReadAtIsNullAndDeletedAtIsNull(
                        organizationId, tenantId, NotificationChannel.IN_APP, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMyAnnouncements(Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID tenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant profile not found");
        }

        return notificationRepository
                .findByOrganizationIdAndTenantIdAndAnnouncementTrueAndDeletedAtIsNull(
                        organizationId, tenantId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnreadNotifications() {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID tenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant profile not found");
        }

        long unreadInvoices = notificationRepository
                .countByOrganizationIdAndTenantIdAndChannelAndInvoiceIdIsNotNullAndReadAtIsNullAndDeletedAtIsNull(
                        organizationId, tenantId, NotificationChannel.IN_APP);
        long unreadAnnouncements = notificationRepository
                .countByOrganizationIdAndTenantIdAndAnnouncementTrueAndReadAtIsNullAndDeletedAtIsNull(
                        organizationId, tenantId);
        return unreadInvoices + unreadAnnouncements;
    }

    @Override
    public void markMyAnnouncementsRead() {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID tenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant profile not found");
        }

        notificationRepository.markAnnouncementsRead(
                organizationId, tenantId, NotificationStatus.READ, java.time.LocalDateTime.now());
    }

    @Override
    public void markMyInvoiceNotificationsRead(UUID invoiceId) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID tenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant profile not found");
        }

        notificationRepository
                .findByOrganizationIdAndTenantIdAndInvoiceIdAndDeletedAtIsNull(organizationId, tenantId, invoiceId)
                .stream()
                .filter(notification -> notification.getChannel() == NotificationChannel.IN_APP)
                .filter(notification -> notification.getReadAt() == null)
                .forEach(notification -> {
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(java.time.LocalDateTime.now());
                });
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
