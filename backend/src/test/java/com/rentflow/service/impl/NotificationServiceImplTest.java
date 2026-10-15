package com.rentflow.service.impl;

import com.rentflow.dto.request.TenantAnnouncementRequest;
import com.rentflow.entity.Notification;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.User;
import com.rentflow.entity.enums.NotificationChannel;
import com.rentflow.entity.enums.NotificationStatus;
import com.rentflow.mapper.NotificationMapper;
import com.rentflow.repository.NotificationRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID organizationId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        User user = User.builder()
                .organizationId(organizationId)
                .tenantId(tenantId)
                .roles(new HashSet<>())
                .build();
        user.setId(UUID.randomUUID());

        UserDetailsImpl userDetails = new UserDetailsImpl(user, new HashSet<>());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void announceToAllTenantsSavesInAppAnnouncementForEveryTenant() {
        Tenant firstTenant = tenant(UUID.randomUUID());
        Tenant secondTenant = tenant(UUID.randomUUID());
        when(tenantRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId))
                .thenReturn(List.of(firstTenant, secondTenant));
        doAnswer(invocation -> {
            List<Notification> notifications = invocation.getArgument(0);
            assertEquals(2, notifications.size());
            assertTrue(notifications.stream().allMatch(notification ->
                    notification.getOrganizationId().equals(organizationId)
                            && notification.isAnnouncement()
                            && notification.getChannel() == NotificationChannel.IN_APP
                            && notification.getStatus() == NotificationStatus.DELIVERED
                            && notification.getSentAt() != null
                            && notification.getDeliveredAt() != null
                            && "Building update".equals(notification.getTitle())
                            && "Water will be unavailable on Friday.".equals(notification.getMessage())));
            assertTrue(notifications.stream().anyMatch(notification ->
                    notification.getTenantId().equals(firstTenant.getId())));
            assertTrue(notifications.stream().anyMatch(notification ->
                    notification.getTenantId().equals(secondTenant.getId())));
            return notifications;
        }).when(notificationRepository).saveAll(anyList());

        TenantAnnouncementRequest request = new TenantAnnouncementRequest();
        request.setTitle("Building update");
        request.setMessage("Water will be unavailable on Friday.");

        int count = notificationService.announceToAllTenants(request);

        assertEquals(2, count);
        verify(tenantRepository).findByOrganizationIdAndDeletedAtIsNull(organizationId);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void announceToAllTenantsDoesNotSaveWhenThereAreNoTenants() {
        when(tenantRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId)).thenReturn(List.of());

        TenantAnnouncementRequest request = new TenantAnnouncementRequest();
        request.setTitle("Building update");
        request.setMessage("Water will be unavailable on Friday.");

        assertEquals(0, notificationService.announceToAllTenants(request));
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void countMyUnreadNotificationsIncludesInvoiceAndAnnouncementNotices() {
        when(notificationRepository
                .countByOrganizationIdAndTenantIdAndChannelAndInvoiceIdIsNotNullAndReadAtIsNullAndDeletedAtIsNull(
                        organizationId, tenantId, NotificationChannel.IN_APP))
                .thenReturn(2L);
        when(notificationRepository
                .countByOrganizationIdAndTenantIdAndAnnouncementTrueAndReadAtIsNullAndDeletedAtIsNull(
                        organizationId, tenantId))
                .thenReturn(3L);

        assertEquals(5L, notificationService.countMyUnreadNotifications());
    }

    @Test
    void markMyAnnouncementsReadScopesUpdateToCurrentTenant() {
        notificationService.markMyAnnouncementsRead();

        verify(notificationRepository).markAnnouncementsRead(
                org.mockito.ArgumentMatchers.eq(organizationId),
                org.mockito.ArgumentMatchers.eq(tenantId),
                org.mockito.ArgumentMatchers.eq(NotificationStatus.READ),
                org.mockito.ArgumentMatchers.any());
    }

    private Tenant tenant(UUID id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setOrganizationId(organizationId);
        return tenant;
    }
}
