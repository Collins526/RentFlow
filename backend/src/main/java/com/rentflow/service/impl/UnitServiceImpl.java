package com.rentflow.service.impl;

import com.rentflow.dto.request.BulkUnitRequest;
import com.rentflow.dto.request.OccupancyStatusRequest;
import com.rentflow.dto.request.UnitRequest;
import com.rentflow.dto.response.UnitResponse;
import com.rentflow.dto.response.UnitSummaryResponse;
import com.rentflow.entity.Block;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.UnitMapper;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private static final String INACTIVE_STATUS = "INACTIVE";

    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;
    private final BlockRepository blockRepository;

    @Override
    @Transactional
    public UnitResponse createUnit(UUID propertyId, UnitRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();
        verifyPropertyOwnership(propertyId, organizationId);

        String unitNumber = request.getUnitNumber().trim();
        if (unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(propertyId, unitNumber)) {
            throw new BadRequestException("Unit number '" + unitNumber + "' already exists in this property");
        }

        Block block = resolveBlock(request.getBlockId(), propertyId, organizationId);

        Unit unit = UnitMapper.toEntity(request, propertyId);
        Unit savedUnit = unitRepository.save(unit);

        return UnitMapper.toDto(savedUnit, block != null ? block.getName() : null);
    }

    @Override
    @Transactional
    public List<UnitResponse> bulkCreateUnits(UUID propertyId, BulkUnitRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();
        verifyPropertyOwnership(propertyId, organizationId);

        Block block = resolveBlock(request.getBlockId(), propertyId, organizationId);

        List<String> generatedNumbers = generateUnitNumbers(request);

        // Reject the whole batch if it collides with itself...
        Set<String> distinct = new LinkedHashSet<>();
        List<String> internalDuplicates = generatedNumbers.stream()
                .filter(number -> !distinct.add(number.toUpperCase()))
                .distinct()
                .collect(Collectors.toList());
        if (!internalDuplicates.isEmpty()) {
            throw new BadRequestException("Generated unit numbers are not unique: "
                    + String.join(", ", internalDuplicates));
        }

        // ...or with units that already exist in this property.
        Set<String> upperCased = generatedNumbers.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> taken = unitRepository.findTakenUnitNumbers(propertyId, upperCased);
        if (!taken.isEmpty()) {
            throw new BadRequestException("These unit numbers already exist in this property: "
                    + String.join(", ", taken));
        }

        List<Unit> units = generatedNumbers.stream()
                .map(number -> UnitMapper.toEntity(request, propertyId, number))
                .collect(Collectors.toList());

        List<Unit> savedUnits = unitRepository.saveAll(units);
        String blockName = block != null ? block.getName() : null;

        return savedUnits.stream()
                .map(unit -> UnitMapper.toDto(unit, blockName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitResponse> getUnitsByPropertyId(UUID propertyId, OccupancyStatus occupancyStatus, Pageable pageable) {
        UUID organizationId = getCurrentUserOrganizationId();
        verifyPropertyOwnership(propertyId, organizationId);

        Page<Unit> units = occupancyStatus != null
                ? unitRepository.findByPropertyIdAndOccupancyStatusAndDeletedAtIsNull(propertyId, occupancyStatus, pageable)
                : unitRepository.findByPropertyIdAndDeletedAtIsNull(propertyId, pageable);

        return toDtoPage(units);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitResponse> getUnitsByBlockId(UUID blockId, Pageable pageable) {
        UUID organizationId = getCurrentUserOrganizationId();
        Block block = blockRepository.findByIdAndOrganizationId(blockId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + blockId));

        Page<Unit> units = unitRepository.findByBlockIdAndDeletedAtIsNull(blockId, pageable);
        return units.map(unit -> UnitMapper.toDto(unit, block.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public UnitSummaryResponse getPropertyUnitSummary(UUID propertyId) {
        UUID organizationId = getCurrentUserOrganizationId();
        verifyPropertyOwnership(propertyId, organizationId);

        long total = 0L;
        BigDecimal totalRent = BigDecimal.ZERO;
        BigDecimal occupiedRent = BigDecimal.ZERO;
        Map<OccupancyStatus, Long> counts = new EnumMap<>(OccupancyStatus.class);

        for (Object[] row : unitRepository.countByOccupancyStatus(propertyId)) {
            OccupancyStatus status = (OccupancyStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal rent = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            counts.put(status, count);
            total += count;
            totalRent = totalRent.add(rent);
            if (status == OccupancyStatus.OCCUPIED) {
                occupiedRent = occupiedRent.add(rent);
            }
        }

        return UnitSummaryResponse.builder()
                .totalUnits(total)
                .vacant(counts.getOrDefault(OccupancyStatus.VACANT, 0L))
                .reserved(counts.getOrDefault(OccupancyStatus.RESERVED, 0L))
                .occupied(counts.getOrDefault(OccupancyStatus.OCCUPIED, 0L))
                .underMaintenance(counts.getOrDefault(OccupancyStatus.UNDER_MAINTENANCE, 0L))
                .totalPotentialRent(totalRent)
                .currentContractedRent(occupiedRent)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getUnitById(UUID id) {
        Unit unit = getUnitEntity(id);
        return UnitMapper.toDto(unit, resolveBlockName(unit.getBlockId()));
    }

    @Override
    @Transactional
    public UnitResponse updateUnit(UUID id, UnitRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();
        Unit unit = getUnitEntity(id, organizationId);

        String unitNumber = request.getUnitNumber().trim();
        if (unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndIdNotAndDeletedAtIsNull(
                unit.getPropertyId(), unitNumber, id)) {
            throw new BadRequestException("Unit number '" + unitNumber + "' already exists in this property");
        }

        // The parent property is immutable; a unit may only move between blocks of that property.
        Block block = resolveBlock(request.getBlockId(), unit.getPropertyId(), organizationId);

        unit.setUnitNumber(unitNumber);
        unit.setType(request.getType());
        unit.setBlockId(request.getBlockId());
        unit.setFloorNumber(request.getFloorNumber());
        unit.setBedrooms(request.getBedrooms() != null ? request.getBedrooms() : 0);
        unit.setBathrooms(request.getBathrooms() != null ? request.getBathrooms() : 0);
        unit.setSizeSqFt(request.getSizeSqFt());
        unit.setRentAmount(request.getRentAmount());
        unit.setDepositAmount(request.getDepositAmount());
        unit.setWaterMeterNumber(request.getWaterMeterNumber());
        unit.setElectricityMeterNumber(request.getElectricityMeterNumber());
        unit.setDescription(request.getDescription());
        if (request.getFurnished() != null) {
            unit.setFurnished(request.getFurnished());
        }
        if (request.getStatus() != null) {
            unit.setStatus(request.getStatus());
        }

        Unit updatedUnit = unitRepository.save(unit);
        return UnitMapper.toDto(updatedUnit, block != null ? block.getName() : null);
    }

    @Override
    @Transactional
    public UnitResponse updateOccupancyStatus(UUID id, OccupancyStatusRequest request) {
        Unit unit = getUnitEntity(id);
        OccupancyStatus target = request.getOccupancyStatus();

        if (target == OccupancyStatus.OCCUPIED) {
            throw new BadRequestException(
                    "A unit can only become OCCUPIED through an active tenancy, not a manual update");
        }

        if (unit.getOccupancyStatus() == OccupancyStatus.OCCUPIED) {
            throw new BadRequestException(
                    "Unit " + unit.getUnitNumber() + " is occupied. End its tenancy before changing occupancy");
        }

        unit.setOccupancyStatus(target);
        Unit updatedUnit = unitRepository.save(unit);
        return UnitMapper.toDto(updatedUnit, resolveBlockName(updatedUnit.getBlockId()));
    }

    @Override
    @Transactional
    public void setOccupiedStatusForTenancy(UUID id, boolean isOccupied) {
        Unit unit = getUnitEntity(id);
        OccupancyStatus newStatus = isOccupied ? OccupancyStatus.OCCUPIED : OccupancyStatus.VACANT;
        
        // Don't overwrite if it's already in the correct state
        if (unit.getOccupancyStatus() != newStatus) {
            unit.setOccupancyStatus(newStatus);
            unitRepository.save(unit);
        }
    }

    @Override
    @Transactional
    public void deleteUnit(UUID id) {
        Unit unit = getUnitEntity(id);

        if (unit.getOccupancyStatus() == OccupancyStatus.OCCUPIED) {
            throw new BadRequestException(
                    "Unit " + unit.getUnitNumber() + " is occupied and cannot be deleted");
        }

        // Soft delete: historical tenancies, ledgers and inspections must keep resolving.
        unit.setDeletedAt(Instant.now());
        unit.setStatus(INACTIVE_STATUS);
        unitRepository.save(unit);
    }

    /**
     * Expands a bulk request into its concrete unit numbers, e.g.
     * prefix "A", start 101, count 3, padding 0 -> [A101, A102, A103].
     */
    private List<String> generateUnitNumbers(BulkUnitRequest request) {
        String prefix = request.getPrefix() != null ? request.getPrefix().trim() : "";
        int padding = request.getNumberPadding() != null ? request.getNumberPadding() : 0;

        List<String> numbers = new ArrayList<>(request.getCount());
        for (int i = 0; i < request.getCount(); i++) {
            String numeric = String.valueOf(request.getStartNumber() + i);
            if (padding > numeric.length()) {
                numeric = "0".repeat(padding - numeric.length()) + numeric;
            }

            String unitNumber = prefix + numeric;
            if (unitNumber.length() > 50) {
                throw new BadRequestException("Generated unit number '" + unitNumber + "' exceeds 50 characters");
            }
            numbers.add(unitNumber);
        }

        return numbers;
    }

    private Page<UnitResponse> toDtoPage(Page<Unit> units) {
        Set<UUID> blockIds = units.getContent().stream()
                .map(Unit::getBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, String> blockNames = blockIds.isEmpty()
                ? Collections.emptyMap()
                : blockRepository.findAllById(blockIds).stream()
                        .collect(Collectors.toMap(Block::getId, Block::getName, (a, b) -> a));

        return units.map(unit -> UnitMapper.toDto(unit,
                unit.getBlockId() != null ? blockNames.get(unit.getBlockId()) : null));
    }

    /**
     * Validates that an optional block belongs to both the current organization and
     * the given property. Returns {@code null} when no block was supplied.
     */
    private Block resolveBlock(UUID blockId, UUID propertyId, UUID organizationId) {
        if (blockId == null) {
            return null;
        }

        Block block = blockRepository.findByIdAndOrganizationId(blockId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + blockId));

        if (!block.getPropertyId().equals(propertyId)) {
            throw new BadRequestException("Block " + block.getName() + " does not belong to this property");
        }

        return block;
    }

    private String resolveBlockName(UUID blockId) {
        if (blockId == null) {
            return null;
        }
        return blockRepository.findById(blockId).map(Block::getName).orElse(null);
    }

    private void verifyPropertyOwnership(UUID propertyId, UUID organizationId) {
        propertyRepository.findByIdAndOrganizationId(propertyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
    }

    private Unit getUnitEntity(UUID id) {
        return getUnitEntity(id, getCurrentUserOrganizationId());
    }

    /**
     * Retrieves a live Unit while verifying it belongs to the current user's organization
     * via its parent Property. This is the cascading multi-tenancy check.
     */
    private Unit getUnitEntity(UUID id, UUID organizationId) {
        return unitRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }

    private UUID getCurrentUserOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UUID orgId = userDetails.getUser().getOrganizationId();

        if (orgId == null) {
            throw new ResourceNotFoundException("User does not belong to any organization");
        }

        return orgId;
    }
}
