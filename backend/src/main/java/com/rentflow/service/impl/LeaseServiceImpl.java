package com.rentflow.service.impl;

import com.rentflow.dto.request.EndLeaseRequest;
import com.rentflow.dto.request.LeaseRequest;
import com.rentflow.dto.response.LeaseResponse;
import com.rentflow.entity.Lease;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.LeaseStatus;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.LeaseMapper;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.LeaseService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseServiceImpl implements LeaseService {

    private static final Set<LeaseStatus> TERMINAL_STATUSES = Collections.unmodifiableSet(
            java.util.EnumSet.of(LeaseStatus.EXPIRED, LeaseStatus.TERMINATED));

    private final LeaseRepository leaseRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final UnitService unitService;

    @Override
    @Transactional
    public LeaseResponse createLease(LeaseRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = getTenantOrThrow(request.getTenantId(), organizationId);
        Unit unit = getUnitOrThrow(request.getUnitId(), organizationId);

        validateDates(request.getStartDate(), request.getEndDate(), request.getStatus());

        Lease lease = Lease.builder()
                .organizationId(organizationId)
                .tenancyId(request.getTenancyId())
                .tenantId(tenant.getId())
                .unitId(unit.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .rentAmount(resolveRent(request.getRentAmount(), unit))
                .securityDepositAmount(resolveDeposit(request.getSecurityDepositAmount(), unit))
                .terms(request.getTerms())
                .build();

        Lease saved = leaseRepository.save(lease);

        if (saved.getStatus() == LeaseStatus.ACTIVE) {
            unitService.setOccupiedStatusForTenancy(unit.getId(), true);
        }

        return LeaseMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public LeaseResponse updateLease(UUID id, LeaseRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Lease lease = getLeaseOrThrow(id, organizationId);

        Tenant tenant = getTenantOrThrow(request.getTenantId(), organizationId);
        Unit unit = getUnitOrThrow(request.getUnitId(), organizationId);

        validateDates(request.getStartDate(), request.getEndDate(), request.getStatus());

        UUID previousUnitId = lease.getUnitId();
        boolean wasActive = lease.getStatus() == LeaseStatus.ACTIVE;
        boolean willBeActive = request.getStatus() == LeaseStatus.ACTIVE;

        lease.setTenancyId(request.getTenancyId());
        lease.setTenantId(tenant.getId());
        lease.setUnitId(unit.getId());
        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        lease.setStatus(request.getStatus());
        lease.setRentAmount(resolveRent(request.getRentAmount(), unit));
        lease.setSecurityDepositAmount(resolveDeposit(request.getSecurityDepositAmount(), unit));
        lease.setTerms(request.getTerms());

        Lease saved = leaseRepository.save(lease);

        syncOccupancy(previousUnitId, unit.getId(), wasActive, willBeActive);

        return LeaseMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaseResponse getLeaseById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Lease lease = getLeaseOrThrow(id, organizationId);

        Tenant tenant = tenantRepository.findById(lease.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(lease.getUnitId()).orElse(null);

        return LeaseMapper.toDto(lease, tenant, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaseResponse> getLeases(LeaseStatus status, UUID tenantId, UUID unitId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        return toDtoPage(findFiltered(organizationId, status, tenantId, unitId, pageable));
    }

    @Override
    @Transactional
    public LeaseResponse endLease(UUID id, EndLeaseRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Lease lease = getLeaseOrThrow(id, organizationId);

        if (TERMINAL_STATUSES.contains(lease.getStatus())) {
            throw new BadRequestException("This lease has already ended");
        }

        if (request.getEndDate().isBefore(lease.getStartDate())) {
            throw new BadRequestException("End date cannot be before the lease start date");
        }

        LeaseStatus terminalStatus = request.getStatus() != null ? request.getStatus() : LeaseStatus.EXPIRED;
        if (!TERMINAL_STATUSES.contains(terminalStatus)) {
            throw new BadRequestException("A lease can only be ended as EXPIRED or TERMINATED");
        }

        boolean wasActive = lease.getStatus() == LeaseStatus.ACTIVE;

        lease.setEndDate(request.getEndDate());
        lease.setStatus(terminalStatus);
        Lease saved = leaseRepository.save(lease);

        if (wasActive) {
            unitService.setOccupiedStatusForTenancy(lease.getUnitId(), false);
        }

        Tenant tenant = tenantRepository.findById(saved.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(saved.getUnitId()).orElse(null);

        return LeaseMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public void deleteLease(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Lease lease = getLeaseOrThrow(id, organizationId);

        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            unitService.setOccupiedStatusForTenancy(lease.getUnitId(), false);
        }

        lease.setDeletedAt(Instant.now());
        leaseRepository.save(lease);
    }

    private Page<Lease> findFiltered(UUID organizationId, LeaseStatus status, UUID tenantId, UUID unitId, Pageable pageable) {
        if (tenantId != null && unitId != null) {
            throw new BadRequestException("Filter by tenant or by unit, not both");
        }

        if (tenantId != null) {
            return status != null
                    ? leaseRepository.findByOrganizationIdAndTenantIdAndStatusAndDeletedAtIsNull(organizationId, tenantId, status, pageable)
                    : leaseRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        }

        if (unitId != null) {
            return status != null
                    ? leaseRepository.findByOrganizationIdAndUnitIdAndStatusAndDeletedAtIsNull(organizationId, unitId, status, pageable)
                    : leaseRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        }

        return status != null
                ? leaseRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, status, pageable)
                : leaseRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
    }

    private Page<LeaseResponse> toDtoPage(Page<Lease> leases) {
        if (leases.isEmpty()) {
            return leases.map(lease -> LeaseMapper.toDto(lease, null, null));
        }

        Map<UUID, Tenant> tenants = loadById(
                leases.getContent().stream().map(Lease::getTenantId).collect(Collectors.toSet()),
                tenantRepository::findAllById, Tenant::getId);

        Map<UUID, Unit> units = loadById(
                leases.getContent().stream().map(Lease::getUnitId).collect(Collectors.toSet()),
                unitRepository::findAllById, Unit::getId);

        return leases.map(lease -> LeaseMapper.toDto(
                lease, tenants.get(lease.getTenantId()), units.get(lease.getUnitId())));
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

    private void validateDates(LocalDate startDate, LocalDate endDate, LeaseStatus status) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before the start date");
        }

        // Basic sanity checks similar to tenancies can be added here if needed
        if (TERMINAL_STATUSES.contains(status) && endDate == null) {
            throw new BadRequestException("A " + status + " lease requires an end date");
        }
    }

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

    private Lease getLeaseOrThrow(UUID id, UUID organizationId) {
        return leaseRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + id));
    }

    private Tenant getTenantOrThrow(UUID id, UUID organizationId) {
        return tenantRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }

    private Unit getUnitOrThrow(UUID id, UUID organizationId) {
        return unitRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }
}
