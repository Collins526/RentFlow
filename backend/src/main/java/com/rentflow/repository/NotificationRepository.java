package com.rentflow.repository;

import com.rentflow.entity.Notification;
import com.rentflow.entity.enums.NotificationChannel;
import com.rentflow.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;


public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    java.util.Optional<Notification> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<Notification> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, NotificationStatus status, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenantIdAndChannelAndInvoiceIdIsNotNullAndReadAtIsNullAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, NotificationChannel channel, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenantIdAndAnnouncementTrueAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, Pageable pageable);

    long countByOrganizationIdAndTenantIdAndChannelAndInvoiceIdIsNotNullAndReadAtIsNullAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, NotificationChannel channel);

    long countByOrganizationIdAndTenantIdAndAnnouncementTrueAndReadAtIsNullAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.status = :status, n.readAt = :readAt
            WHERE n.organizationId = :organizationId
              AND n.tenantId = :tenantId
              AND n.announcement = true
              AND n.readAt IS NULL
              AND n.deletedAt IS NULL
            """)
    int markAnnouncementsRead(
            @Param("organizationId") UUID organizationId,
            @Param("tenantId") UUID tenantId,
            @Param("status") NotificationStatus status,
            @Param("readAt") LocalDateTime readAt);

    List<Notification> findByOrganizationIdAndTenantIdAndInvoiceIdAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, UUID invoiceId);
}
