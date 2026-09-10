package com.rentflow.repository;

import com.rentflow.entity.Tenancy;
import com.rentflow.entity.enums.TenancyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;


public interface TenancyRepository extends JpaRepository<Tenancy, UUID> {

    Optional<Tenancy> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Page<Tenancy> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    Page<Tenancy> findByOrganizationIdAndStatusAndDeletedAtIsNull(
            UUID organizationId, TenancyStatus status, Pageable pageable);

    Page<Tenancy> findByOrganizationIdAndTenantIdAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, Pageable pageable);

    Page<Tenancy> findByOrganizationIdAndTenantIdAndStatusAndDeletedAtIsNull(
            UUID organizationId, UUID tenantId, TenancyStatus status, Pageable pageable);

    Page<Tenancy> findByOrganizationIdAndUnitIdAndDeletedAtIsNull(
            UUID organizationId, UUID unitId, Pageable pageable);

    Page<Tenancy> findByOrganizationIdAndUnitIdAndStatusAndDeletedAtIsNull(
            UUID organizationId, UUID unitId, TenancyStatus status, Pageable pageable);

    Optional<Tenancy> findByUnitIdAndStatusAndDeletedAtIsNull(UUID unitId, TenancyStatus status);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, TenancyStatus status);

        long countByStatusAndDeletedAtIsNull(TenancyStatus status);

            @Modifying
            @Query("UPDATE Tenancy t SET t.deletedAt = :deletedAt WHERE t.organizationId = :organizationId AND t.deletedAt IS NULL")
            int softDeleteByOrganizationId(@Param("organizationId") UUID organizationId, @Param("deletedAt") Instant deletedAt);

            @Query("SELECT COUNT(t) FROM Tenancy t JOIN Organization o ON t.organizationId = o.id "
                    + "WHERE o.deletedAt IS NULL AND t.status = :status AND t.deletedAt IS NULL")
            long countForActiveOrganizationsByStatus(@Param("status") TenancyStatus status);

    /**
     * Rent contracted through tenancies in a given status. Returns 0 rather than null
     * when the organization has none.
     */
    @Query("SELECT COALESCE(SUM(t.rentAmount), 0) FROM Tenancy t "
            + "WHERE t.organizationId = :organizationId AND t.status = :status AND t.deletedAt IS NULL")
    BigDecimal sumRentByStatus(@Param("organizationId") UUID organizationId,
                               @Param("status") TenancyStatus status);

    @Query("SELECT COALESCE(SUM(t.rentAmount), 0) FROM Tenancy t JOIN Organization o ON t.organizationId = o.id "
            + "WHERE o.deletedAt IS NULL AND t.status = :status AND t.deletedAt IS NULL")
    BigDecimal sumRentForAllOrganizationsByStatus(@Param("status") TenancyStatus status);

    /**
     * Finds live tenancies on a unit whose occupancy window intersects
     * {@code [startDate, endDate]}. Callers substitute a far-future sentinel for an
     * open-ended date on both sides, which keeps every bind parameter non-null and
     * lets a single inclusive comparison cover the open-ended cases.
     * <p>
     * Only {@code statuses} that reserve the unit (UPCOMING, ACTIVE) should be passed;
     * a PAST or EVICTED tenancy never blocks a new one.
     */
    @Query("SELECT t FROM Tenancy t "
            + "WHERE t.unitId = :unitId "
            + "AND t.deletedAt IS NULL "
            + "AND t.status IN :statuses "
            + "AND t.startDate <= :endDate "
            + "AND COALESCE(t.endDate, :openEnded) >= :startDate")
    List<Tenancy> findOverlapping(@Param("unitId") UUID unitId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("openEnded") LocalDate openEnded,
                                  @Param("statuses") Collection<TenancyStatus> statuses);
}
