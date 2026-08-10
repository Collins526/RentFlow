package com.rentflow.service.impl;

import com.rentflow.dto.request.BulkUnitRequest;
import com.rentflow.dto.request.OccupancyStatusRequest;
import com.rentflow.dto.request.UnitRequest;
import com.rentflow.dto.response.UnitResponse;
import com.rentflow.dto.response.UnitSummaryResponse;
import com.rentflow.entity.Block;
import com.rentflow.entity.Property;
import com.rentflow.entity.Unit;
import com.rentflow.entity.User;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.UnitType;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private UnitServiceImpl unitService;

    private UUID orgId;
    private UUID propertyId;
    private UUID blockId;
    private UUID unitId;
    private Property mockProperty;
    private Block mockBlock;
    private Unit mockUnit;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        blockId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        mockProperty = Property.builder()
                .organizationId(orgId)
                .name("Kilimani Court")
                .type("RESIDENTIAL")
                .address("123 Test St")
                .status("ACTIVE")
                .build();
        mockProperty.setId(propertyId);

        mockBlock = Block.builder()
                .propertyId(propertyId)
                .name("Building A")
                .numberOfFloors(5)
                .build();
        mockBlock.setId(blockId);

        mockUnit = Unit.builder()
                .propertyId(propertyId)
                .blockId(blockId)
                .unitNumber("A101")
                .type(UnitType.TWO_BEDROOM)
                .floorNumber(1)
                .bedrooms(2)
                .bathrooms(1)
                .rentAmount(new BigDecimal("45000.00"))
                .depositAmount(new BigDecimal("45000.00"))
                .occupancyStatus(OccupancyStatus.VACANT)
                .status("ACTIVE")
                .build();
        mockUnit.setId(unitId);

        User mockUser = User.builder()
                .email("owner@testorg.com")
                .organizationId(orgId)
                .roles(new HashSet<>())
                .build();
        mockUser.setId(UUID.randomUUID());

        UserDetailsImpl userDetails = new UserDetailsImpl(mockUser, new HashSet<>());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UnitRequest buildRequest(String unitNumber) {
        UnitRequest request = new UnitRequest();
        request.setUnitNumber(unitNumber);
        request.setType(UnitType.ONE_BEDROOM);
        request.setBedrooms(1);
        request.setBathrooms(1);
        request.setRentAmount(new BigDecimal("30000.00"));
        return request;
    }

    /** Mirrors {@code saveAll} by echoing back the entities it was handed. */
    private static org.mockito.stubbing.Answer<List<Unit>> echoSaveAll() {
        return invocation -> {
            List<Unit> units = new java.util.ArrayList<>();
            ((Iterable<?>) invocation.getArguments()[0]).forEach(u -> units.add((Unit) u));
            return units;
        };
    }

    private BulkUnitRequest buildBulkRequest(String prefix, int start, int count, int padding) {
        BulkUnitRequest request = new BulkUnitRequest();
        request.setPrefix(prefix);
        request.setStartNumber(start);
        request.setCount(count);
        request.setNumberPadding(padding);
        request.setType(UnitType.BEDSITTER);
        request.setRentAmount(new BigDecimal("12000.00"));
        return request;
    }

    @Test
    void createUnit_Success() {
        UnitRequest request = buildRequest("B201");

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(propertyId, "B201"))
                .thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(i -> {
            Unit u = (Unit) i.getArguments()[0];
            u.setId(UUID.randomUUID());
            return u;
        });

        UnitResponse response = unitService.createUnit(propertyId, request);

        assertNotNull(response);
        assertEquals("B201", response.getUnitNumber());
        assertEquals(UnitType.ONE_BEDROOM, response.getType());
        assertEquals(propertyId, response.getPropertyId());
        // New units always start vacant, regardless of what the caller sends.
        assertEquals(OccupancyStatus.VACANT, response.getOccupancyStatus());
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void createUnit_WithBlock_ResolvesBlockName() {
        UnitRequest request = buildRequest("A102");
        request.setBlockId(blockId);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(propertyId, "A102"))
                .thenReturn(false);
        when(blockRepository.findByIdAndOrganizationId(blockId, orgId))
                .thenReturn(Optional.of(mockBlock));
        when(unitRepository.save(any(Unit.class))).thenAnswer(i -> i.getArguments()[0]);

        UnitResponse response = unitService.createUnit(propertyId, request);

        assertEquals(blockId, response.getBlockId());
        assertEquals("Building A", response.getBlockName());
    }

    @Test
    void createUnit_DuplicateUnitNumber_ThrowsBadRequest() {
        UnitRequest request = buildRequest("A101");

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(propertyId, "A101"))
                .thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> unitService.createUnit(propertyId, request));

        assertTrue(ex.getMessage().contains("A101"));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void createUnit_BlockFromAnotherProperty_ThrowsBadRequest() {
        UnitRequest request = buildRequest("C301");
        request.setBlockId(blockId);

        Block foreignBlock = Block.builder()
                .propertyId(UUID.randomUUID())
                .name("Foreign Block")
                .build();
        foreignBlock.setId(blockId);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndDeletedAtIsNull(propertyId, "C301"))
                .thenReturn(false);
        when(blockRepository.findByIdAndOrganizationId(blockId, orgId))
                .thenReturn(Optional.of(foreignBlock));

        assertThrows(BadRequestException.class, () -> unitService.createUnit(propertyId, request));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void createUnit_PropertyOutsideOrganization_ThrowsNotFound() {
        UnitRequest request = buildRequest("D401");
        UUID foreignPropertyId = UUID.randomUUID();

        when(propertyRepository.findByIdAndOrganizationId(foreignPropertyId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.createUnit(foreignPropertyId, request));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void bulkCreateUnits_GeneratesSequentialNumbers() {
        BulkUnitRequest request = buildBulkRequest("A", 101, 3, 0);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.findTakenUnitNumbers(eq(propertyId), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(unitRepository.saveAll(any())).thenAnswer(echoSaveAll());

        List<UnitResponse> responses = unitService.bulkCreateUnits(propertyId, request);

        assertEquals(3, responses.size());
        assertEquals("A101", responses.get(0).getUnitNumber());
        assertEquals("A102", responses.get(1).getUnitNumber());
        assertEquals("A103", responses.get(2).getUnitNumber());
        assertTrue(responses.stream().allMatch(u -> u.getOccupancyStatus() == OccupancyStatus.VACANT));
    }

    @Test
    void bulkCreateUnits_AppliesZeroPadding() {
        BulkUnitRequest request = buildBulkRequest("GF-", 1, 2, 3);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.findTakenUnitNumbers(eq(propertyId), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(unitRepository.saveAll(any())).thenAnswer(echoSaveAll());

        List<UnitResponse> responses = unitService.bulkCreateUnits(propertyId, request);

        assertEquals("GF-001", responses.get(0).getUnitNumber());
        assertEquals("GF-002", responses.get(1).getUnitNumber());
    }

    @Test
    void bulkCreateUnits_ExistingNumberCollision_AbortsEntireBatch() {
        BulkUnitRequest request = buildBulkRequest("A", 101, 5, 0);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.findTakenUnitNumbers(eq(propertyId), anyCollection()))
                .thenReturn(List.of("A103"));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> unitService.bulkCreateUnits(propertyId, request));

        assertTrue(ex.getMessage().contains("A103"));
        verify(unitRepository, never()).saveAll(any());
    }

    @Test
    void getUnitsByPropertyId_Success() {
        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.findByPropertyIdAndDeletedAtIsNull(eq(propertyId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockUnit)));
        when(blockRepository.findAllById(anyCollection()))
                .thenReturn(Collections.singletonList(mockBlock));

        Page<UnitResponse> result = unitService.getUnitsByPropertyId(propertyId, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("A101", result.getContent().get(0).getUnitNumber());
        assertEquals("Building A", result.getContent().get(0).getBlockName());
    }

    @Test
    void getUnitsByPropertyId_FiltersByOccupancyStatus() {
        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.findByPropertyIdAndOccupancyStatusAndDeletedAtIsNull(
                eq(propertyId), eq(OccupancyStatus.VACANT), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockUnit)));
        when(blockRepository.findAllById(anyCollection()))
                .thenReturn(Collections.singletonList(mockBlock));

        Page<UnitResponse> result = unitService.getUnitsByPropertyId(
                propertyId, OccupancyStatus.VACANT, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(unitRepository, never()).findByPropertyIdAndDeletedAtIsNull(any(), any());
    }

    @Test
    void getUnitById_CrossOrganization_ThrowsNotFound() {
        UUID foreignUnitId = UUID.randomUUID();
        when(unitRepository.findByIdAndOrganizationId(foreignUnitId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.getUnitById(foreignUnitId));
    }

    @Test
    void getPropertyUnitSummary_AggregatesByStatus() {
        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(unitRepository.countByOccupancyStatus(propertyId)).thenReturn(List.of(
                new Object[]{OccupancyStatus.VACANT, 2L, new BigDecimal("60000.00")},
                new Object[]{OccupancyStatus.OCCUPIED, 3L, new BigDecimal("135000.00")}
        ));

        UnitSummaryResponse summary = unitService.getPropertyUnitSummary(propertyId);

        assertEquals(5, summary.getTotalUnits());
        assertEquals(2, summary.getVacant());
        assertEquals(3, summary.getOccupied());
        assertEquals(0, summary.getReserved());
        assertEquals(0, summary.getUnderMaintenance());
        assertEquals(0, new BigDecimal("195000.00").compareTo(summary.getTotalPotentialRent()));
        assertEquals(0, new BigDecimal("135000.00").compareTo(summary.getCurrentContractedRent()));
    }

    @Test
    void updateUnit_Success() {
        UnitRequest request = buildRequest("A101-R");
        request.setRentAmount(new BigDecimal("52000.00"));

        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndIdNotAndDeletedAtIsNull(
                propertyId, "A101-R", unitId)).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(i -> i.getArguments()[0]);

        UnitResponse response = unitService.updateUnit(unitId, request);

        assertEquals("A101-R", response.getUnitNumber());
        assertEquals(0, new BigDecimal("52000.00").compareTo(response.getRentAmount()));
        // blockId was cleared by the request, so the unit now sits directly under the property.
        assertNull(response.getBlockId());
    }

    @Test
    void updateUnit_DuplicateUnitNumber_ThrowsBadRequest() {
        UnitRequest request = buildRequest("A102");

        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));
        when(unitRepository.existsByPropertyIdAndUnitNumberIgnoreCaseAndIdNotAndDeletedAtIsNull(
                propertyId, "A102", unitId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> unitService.updateUnit(unitId, request));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateOccupancyStatus_ToOccupied_IsRejected() {
        OccupancyStatusRequest request = new OccupancyStatusRequest();
        request.setOccupancyStatus(OccupancyStatus.OCCUPIED);

        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> unitService.updateOccupancyStatus(unitId, request));

        assertTrue(ex.getMessage().contains("tenancy"));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateOccupancyStatus_AwayFromOccupied_IsRejected() {
        mockUnit.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        OccupancyStatusRequest request = new OccupancyStatusRequest();
        request.setOccupancyStatus(OccupancyStatus.VACANT);

        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));

        assertThrows(BadRequestException.class, () -> unitService.updateOccupancyStatus(unitId, request));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateOccupancyStatus_ToUnderMaintenance_Success() {
        OccupancyStatusRequest request = new OccupancyStatusRequest();
        request.setOccupancyStatus(OccupancyStatus.UNDER_MAINTENANCE);

        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));
        when(blockRepository.findById(blockId)).thenReturn(Optional.of(mockBlock));
        when(unitRepository.save(any(Unit.class))).thenAnswer(i -> i.getArguments()[0]);

        UnitResponse response = unitService.updateOccupancyStatus(unitId, request);

        assertEquals(OccupancyStatus.UNDER_MAINTENANCE, response.getOccupancyStatus());
    }

    @Test
    void deleteUnit_SoftDeletesInsteadOfRemoving() {
        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));
        when(unitRepository.save(any(Unit.class))).thenAnswer(i -> i.getArguments()[0]);

        unitService.deleteUnit(unitId);

        assertNotNull(mockUnit.getDeletedAt());
        assertEquals("INACTIVE", mockUnit.getStatus());
        verify(unitRepository).save(mockUnit);
        verify(unitRepository, never()).delete(any());
    }

    @Test
    void deleteUnit_OccupiedUnit_ThrowsBadRequest() {
        mockUnit.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));

        assertThrows(BadRequestException.class, () -> unitService.deleteUnit(unitId));

        assertNull(mockUnit.getDeletedAt());
        verify(unitRepository, never()).save(any());
    }
}
