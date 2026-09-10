package com.rentflow.repository;

import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.OccupancyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UnitRepository extends JpaRepository<Unit, UUID> {

    /**
     * Cascading multi-tenancy check: resolves a unit only when its parent property
     * belongs to the given organization. Soft-deleted units are invisible.
     */
    @Query("SELECT u FROM Unit u JOIN Property p ON u.propertyId = p.id " +
           "WHERE u.id = :unitId AND p.organizationId = :organizationId AND u.deletedAt IS NULL")
    Optional<Unit> findByIdAndOrganizationId(@Param("unitId") UUID unitId,
                                            @Param("organizationId") UUID organizationId);

    Page<Unit> findByPropertyIdAndDeletedAtIsNull(UUID propertyId, Pageable pageable);

        long countByPropertyIdAndDeletedAtIsNull(UUID propertyId);

    Page<Unit> findByPropertyIdAndOccupancyStatusAndDeletedAtIsNull(
            UUID propertyId, OccupancyStatus occupancyStatus, Pageable pageable);

    Page<Unit> findByBlockIdAndDeletedAtIsNull(UUID blockId, Pageable pageable);

    boolean existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(UUID propertyId, String unitNumber);

    /**
     * Duplicate check for updates, where the unit being edited must not collide with itself.
     */
    boolean existsByPropertyIdAndUnitNumberIgnoreCaseAndIdNotAndDeletedAtIsNull(
            UUID propertyId, String unitNumber, UUID id);

    /**
     * Resolves which of the supplied numbers are already taken within a property.
     * Used to validate an entire bulk batch before a single row is inserted.
     */
    @Query("SELECT u.unitNumber FROM Unit u WHERE u.propertyId = :propertyId " +
           "AND u.deletedAt IS NULL AND UPPER(u.unitNumber) IN :unitNumbers")
    List<String> findTakenUnitNumbers(@Param("propertyId") UUID propertyId,
                                      @Param("unitNumbers") Collection<String> unitNumbers);

    @Query("SELECT u.occupancyStatus, COUNT(u), COALESCE(SUM(u.rentAmount), 0) FROM Unit u " +
           "WHERE u.propertyId = :propertyId AND u.deletedAt IS NULL GROUP BY u.occupancyStatus")
    List<Object[]> countByOccupancyStatus(@Param("propertyId") UUID propertyId);

    /**
     * Same aggregation as {@link #countByOccupancyStatus} but across the whole
     * organization, reached through each unit's parent property.
     */
    @Query("SELECT u.occupancyStatus, COUNT(u), COALESCE(SUM(u.rentAmount), 0) FROM Unit u "
            + "JOIN Property p ON u.propertyId = p.id "
            + "WHERE p.organizationId = :organizationId AND u.deletedAt IS NULL "
            + "GROUP BY u.occupancyStatus")
    List<Object[]> countByOccupancyStatusForOrganization(@Param("organizationId") UUID organizationId);

        @Query("SELECT u.occupancyStatus, COUNT(u), COALESCE(SUM(u.rentAmount), 0) FROM Unit u "
                        + "JOIN Property p ON u.propertyId = p.id "
                        + "WHERE p.deletedAt IS NULL AND u.deletedAt IS NULL "
                        + "GROUP BY u.occupancyStatus")
        List<Object[]> countByOccupancyStatusForAllOrganizations();

        @Query("SELECT COUNT(u) FROM Unit u JOIN Property p ON u.propertyId = p.id "
                        + "WHERE p.organizationId = :organizationId AND p.deletedAt IS NULL AND u.deletedAt IS NULL")
        long countByOrganizationId(@Param("organizationId") UUID organizationId);
}
