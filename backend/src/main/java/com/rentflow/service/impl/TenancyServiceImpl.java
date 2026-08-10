package com.rentflow.service.impl;

import com.rentflow.dto.request.EndTenancyRequest;
import com.rentflow.dto.request.TenancyRequest;
import com.rentflow.dto.response.TenancyResponse;
import com.rentflow.entity.Tenancy;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.TenancyMapper;
import com.rentflow.repository.TenancyRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.TenancyService;
import com.rentflow.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenancyServiceImpl implements TenancyService {

    /**
     * Stand-in for "no end date" so overlap comparisons stay a single inclusive test
     * instead of branching on nulls in JPQL.
     */
    private static final LocalDate OPEN_ENDED = LocalDate.of(9999, 12, 31);

    /** Statuses that reserve a unit and therefore cannot overlap on it. */
    private static final Set<TenancyStatus> RESERVING_STATUSES =
            Collections.unmodifiableSet(EnumSet.of(TenancyStatus.UPCOMING, TenancyStatus.ACTIVE));

    /** Statuses that close a tenancy out. */
    private static final Set<TenancyStatus> TERMINAL_STATUSES =
            Collections.unmodifiableSet(EnumSet.of(TenancyStatus.PAST, TenancyStatus.EVICTED));

    private final TenancyRepository tenancyRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final UnitService unitService;

    @Override
    @Transactional
    public TenancyResponse createTenancy(TenancyRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = getTenantOrThrow(request.getTenantId(), organizationId);
        Unit unit = getUnitOrThrow(request.getUnitId(), organizationId);

        validateDates(request.getStartDate(), request.getEndDate(), request.getStatus());
        validateUnitIsAvailable(unit, request.getStatus());
        validateNoOverlap(unit, request.getStartDate(), request.getEndDate(), request.getStatus(), null);

        Tenancy tenancy = Tenancy.builder()
                .organizationId(organizationId)
                .tenantId(tenant.getId())
                .unitId(unit.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .rentAmount(resolveRent(request.getRentAmount(), unit))
                .securityDepositAmount(resolveDeposit(request.getSecurityDepositAmount(), unit))
                .build();

        Tenancy saved = tenancyRepository.save(tenancy);

        if (saved.getStatus() == TenancyStatus.ACTIVE) {
            unitService.setOccupiedStatusForTenancy(unit.getId(), true);
        }

        return TenancyMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public TenancyResponse updateTenancy(UUID id, TenancyRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Tenancy tenancy = getTenancyOrThrow(id, organizationId);

        Tenant tenant = getTenantOrThrow(request.getTenantId(), organizationId);
        Unit unit = getUnitOrThrow(request.getUnitId(), organizationId);

        validateDates(request.getStartDate(), request.getEndDate(), request.getStatus());

        UUID previousUnitId = tenancy.getUnitId();
        boolean wasActive = tenancy.getStatus() == TenancyStatus.ACTIVE;
        boolean willBeActive = request.getStatus() == TenancyStatus.ACTIVE;
        boolean movingUnit = !previousUnitId.equals(unit.getId());

        // A unit already held by *this* tenancy is not a conflict, so availability is
        // only re-checked when the tenancy moves to a different unit or newly activates.
        if (movingUnit || (!wasActive && willBeActive)) {
            validateUnitIsAvailable(unit, request.getStatus());
        }
        validateNoOverlap(unit, request.getStartDate(), request.getEndDate(), request.getStatus(), id);

        tenancy.setTenantId(tenant.getId());
        tenancy.setUnitId(unit.getId());
        tenancy.setStartDate(request.getStartDate());
        tenancy.setEndDate(request.getEndDate());
        tenancy.setStatus(request.getStatus());
        tenancy.setRentAmount(resolveRent(request.getRentAmount(), unit));
        tenancy.setSecurityDepositAmount(resolveDeposit(request.getSecurityDepositAmount(), unit));

        Tenancy saved = tenancyRepository.save(tenancy);

        syncOccupancy(previousUnitId, unit.getId(), wasActive, willBeActive);

        return TenancyMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public TenancyResponse getTenancyById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Tenancy tenancy = getTenancyOrThrow(id, organizationId);

        Tenant tenant = tenantRepository.findById(tenancy.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(tenancy.getUnitId()).orElse(null);

        return TenancyMapper.toDto(tenancy, tenant, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenancyResponse> getTenancies(TenancyStatus status, UUID tenantId, UUID unitId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        return toDtoPage(findFiltered(organizationId, status, tenantId, unitId, pageable));
    }

    @Override
    @Transactional
    public TenancyResponse endTenancy(UUID id, EndTenancyRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Tenancy tenancy = getTenancyOrThrow(id, organizationId);

        if (TERMINAL_STATUSES.contains(tenancy.getStatus())) {
            throw new BadRequestException("This tenancy has already ended");
        }

        if (request.getEndDate().isBefore(tenancy.getStartDate())) {
            throw new BadRequestException("End date cannot be before the tenancy start date");
        }

        TenancyStatus terminalStatus = request.getStatus() != null ? request.getStatus() : TenancyStatus.PAST;
        if (!TERMINAL_STATUSES.contains(terminalStatus)) {
            throw new BadRequestException("A tenancy can only be ended as PAST or EVICTED");
        }

        boolean wasActive = tenancy.getStatus() == TenancyStatus.ACTIVE;

        tenancy.setEndDate(request.getEndDate());
        tenancy.setStatus(terminalStatus);
        Tenancy saved = tenancyRepository.save(tenancy);

        if (wasActive) {
            unitService.setOccupiedStatusForTenancy(tenancy.getUnitId(), false);
        }

        Tenant tenant = tenantRepository.findById(saved.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(saved.getUnitId()).orElse(null);

        return TenancyMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public void deleteTenancy(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Tenancy tenancy = getTenancyOrThrow(id, organizationId);

        // Soft delete: rent ledgers, deposits and inspections reference this row and
        // must keep resolving. Releasing the unit first keeps occupancy truthful.
        if (tenancy.getStatus() == TenancyStatus.ACTIVE) {
            unitService.setOccupiedStatusForTenancy(tenancy.getUnitId(), false);
        }

        tenancy.setDeletedAt(Instant.now());
        tenancyRepository.save(tenancy);
    }

    private Page<Tenancy> findFiltered(UUID organizationId, TenancyStatus status,
                                       UUID tenantId, UUID unitId, Pageable pageable) {
        if (tenantId != null && unitId != null) {
            throw new BadRequestException("Filter by tenant or by unit, not both");
        }

        if (tenantId != null) {
            return status != null
                    ? tenancyRepository.findByOrganizationIdAndTenantIdAndStatusAndDeletedAtIsNull(
                            organizationId, tenantId, status, pageable)
                    : tenancyRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(
                            organizationId, tenantId, pageable);
        }

        if (unitId != null) {
            return status != null
                    ? tenancyRepository.findByOrganizationIdAndUnitIdAndStatusAndDeletedAtIsNull(
                            organizationId, unitId, status, pageable)
                    : tenancyRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(
                            organizationId, unitId, pageable);
        }

        return status != null
                ? tenancyRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, status, pageable)
                : tenancyRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
    }

    /**
     * Resolves tenant and unit names for a whole page in two queries rather than two
     * per row.
     */
    private Page<TenancyResponse> toDtoPage(Page<Tenancy> tenancies) {
        if (tenancies.isEmpty()) {
            return tenancies.map(tenancy -> TenancyMapper.toDto(tenancy, null, null));
        }

        Map<UUID, Tenant> tenants = loadById(
                tenancies.getContent().stream().map(Tenancy::getTenantId).collect(Collectors.toSet()),
                tenantRepository::findAllById, Tenant::getId);

        Map<UUID, Unit> units = loadById(
                tenancies.getContent().stream().map(Tenancy::getUnitId).collect(Collectors.toSet()),
                unitRepository::findAllById, Unit::getId);

        return tenancies.map(tenancy -> TenancyMapper.toDto(
                tenancy, tenants.get(tenancy.getTenantId()), units.get(tenancy.getUnitId())));
    }

    private <T> Map<UUID, T> loadById(Set<UUID> ids,
                                      Function<Set<UUID>, List<T>> loader,
                                      Function<T, UUID> idExtractor) {
        Set<UUID> present = ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (present.isEmpty()) {
            return Collections.emptyMap();
        }

        return loader.apply(present).stream()
                .collect(Collectors.toMap(idExtractor, Function.identity(), (a, b) -> a));
    }

    /**
     * Keeps the status field and the date range describing the same reality. Rent
     * billing derives charge periods from these, so an ACTIVE tenancy that has not
     * started, or one that ended last year, would silently produce wrong invoices.
     */
    private void validateDates(LocalDate startDate, LocalDate endDate, TenancyStatus status) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before the start date");
        }

        LocalDate today = LocalDate.now();

        if (status == TenancyStatus.ACTIVE) {
            if (startDate.isAfter(today)) {
                throw new BadRequestException(
                        "A tenancy starting in the future must be created as UPCOMING");
            }
            if (endDate != null && endDate.isBefore(today)) {
                throw new BadRequestException(
                        "This tenancy's end date has passed; record it as PAST instead of ACTIVE");
            }
        }

        if (status == TenancyStatus.UPCOMING && !startDate.isAfter(today)) {
            throw new BadRequestException(
                    "An UPCOMING tenancy must start in the future; use ACTIVE for one already running");
        }

        if (TERMINAL_STATUSES.contains(status) && endDate == null) {
            throw new BadRequestException("A " + status + " tenancy requires an end date");
        }
    }

    /**
     * Guards the unit's physical state. Overlap detection covers tenancy records;
     * this covers a unit taken out of service for maintenance.
     */
    private void validateUnitIsAvailable(Unit unit, TenancyStatus status) {
        if (!RESERVING_STATUSES.contains(status)) {
            return;
        }

        if (unit.getOccupancyStatus() == OccupancyStatus.UNDER_MAINTENANCE) {
            throw new BadRequestException(
                    "Unit " + unit.getUnitNumber() + " is under maintenance and cannot be let");
        }

        if (status == TenancyStatus.ACTIVE && unit.getOccupancyStatus() == OccupancyStatus.OCCUPIED) {
            throw new BadRequestException("Unit " + unit.getUnitNumber() + " is already occupied");
        }
    }

    /**
     * Rejects a tenancy whose occupancy window collides with another live tenancy on
     * the same unit. Terminated tenancies are ignored, so re-letting a unit after a
     * tenant moves out is always allowed.
     */
    private void validateNoOverlap(Unit unit, LocalDate startDate, LocalDate endDate,
                                   TenancyStatus status, UUID excludeTenancyId) {
        if (!RESERVING_STATUSES.contains(status)) {
            return;
        }

        List<Tenancy> conflicts = tenancyRepository.findOverlapping(
                unit.getId(),
                startDate,
                endDate != null ? endDate : OPEN_ENDED,
                OPEN_ENDED,
                RESERVING_STATUSES);

        conflicts.stream()
                .filter(conflict -> !conflict.getId().equals(excludeTenancyId))
                .findFirst()
                .ifPresent(conflict -> {
                    throw new BadRequestException(String.format(
                            "Unit %s already has a %s tenancy from %s to %s",
                            unit.getUnitNumber(),
                            conflict.getStatus(),
                            conflict.getStartDate(),
                            conflict.getEndDate() != null ? conflict.getEndDate() : "open-ended"));
                });
    }

    /**
     * Applies the occupancy consequences of a status or unit change. Order matters
     * when a tenancy moves between units: release the old one before claiming the new.
     */
    private void syncOccupancy(UUID previousUnitId, UUID newUnitId, boolean wasActive, boolean isActive) {
        boolean movedUnit = !previousUnitId.equals(newUnitId);

        if (wasActive && (!isActive || movedUnit)) {
            unitService.setOccupiedStatusForTenancy(previousUnitId, false);
        }

        if (isActive && (!wasActive || movedUnit)) {
            unitService.setOccupiedStatusForTenancy(newUnitId, true);
        }
    }

    private BigDecimal resolveRent(BigDecimal requested, Unit unit) {
        return requested != null ? requested : unit.getRentAmount();
    }

    private BigDecimal resolveDeposit(BigDecimal requested, Unit unit) {
        return requested != null ? requested : unit.getDepositAmount();
    }

    private Tenancy getTenancyOrThrow(UUID id, UUID organizationId) {
        return tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenancy not found with id: " + id));
    }

    private Tenant getTenantOrThrow(UUID id, UUID organizationId) {
        return tenantRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }

    /** Cascading multi-tenancy: a unit resolves only through a property in this organization. */
    private Unit getUnitOrThrow(UUID id, UUID organizationId) {
        return unitRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }
}
