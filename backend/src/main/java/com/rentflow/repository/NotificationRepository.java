package com.rentflow.repository;

import com.rentflow.entity.Notification;
import com.rentflow.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    java.util.Optional<Notification> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<Notification> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(UUID organizationId, UUID tenancyId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(UUID organizationId, UUID unitId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, NotificationStatus status, Pageable pageable);
}
