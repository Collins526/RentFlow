package com.rentflow.service.impl;

import com.rentflow.dto.request.EndTenancyRequest;
import com.rentflow.dto.request.TenancyRequest;
import com.rentflow.dto.response.TenancyResponse;
import com.rentflow.entity.Tenancy;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.User;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.entity.enums.TenantType;
import com.rentflow.entity.enums.UnitType;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.repository.TenancyRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.service.UnitService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenancyServiceImplTest {

    @Mock
    private TenancyRepository tenancyRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitService unitService;

    @InjectMocks
    private TenancyServiceImpl tenancyService;

    private UUID orgId;
    private UUID propertyId;
    private UUID tenantId;
    private UUID unitId;
    private UUID tenancyId;

    private Tenant mockTenant;
    private Unit mockUnit;
    private Tenancy mockTenancy;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        tenancyId = UUID.randomUUID();

        mockTenant = Tenant.builder()
                .organizationId(orgId)
                .tenantType(TenantType.INDIVIDUAL)
                .firstName("Wanjiru")
                .lastName("Kamau")
                .email("wanjiru@example.com")
                .phoneNumber("+254700000000")
                .status("ACTIVE")
                .build();
        mockTenant.setId(tenantId);

        mockUnit = Unit.builder()
                .propertyId(propertyId)
                .unitNumber("A101")
                .type(UnitType.TWO_BEDROOM)
                .bedrooms(2)
                .bathrooms(1)
                .rentAmount(new BigDecimal("45000.00"))
                .depositAmount(new BigDecimal("90000.00"))
                .occupancyStatus(OccupancyStatus.VACANT)
                .status("ACTIVE")
                .build();
        mockUnit.setId(unitId);

        mockTenancy = Tenancy.builder()
                .organizationId(orgId)
                .tenantId(tenantId)
                .unitId(unitId)
                .startDate(LocalDate.now().minusMonths(6))
                .status(TenancyStatus.ACTIVE)
                .rentAmount(new BigDecimal("45000.00"))
                .securityDepositAmount(new BigDecimal("90000.00"))
                .build();
        mockTenancy.setId(tenancyId);

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

    private TenancyRequest buildRequest(LocalDate start, LocalDate end, TenancyStatus status) {
        TenancyRequest request = new TenancyRequest();
        request.setTenantId(tenantId);
        request.setUnitId(unitId);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setStatus(status);
        return request;
    }

    private void stubTenantAndUnitLookups() {
        when(tenantRepository.findByIdAndOrganizationId(tenantId, orgId)).thenReturn(Optional.of(mockTenant));
        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.of(mockUnit));
    }

    private void stubNoOverlap() {
        when(tenancyRepository.findOverlapping(eq(unitId), any(), any(), any(), anyCollection()))
                .thenReturn(Collections.emptyList());
    }

    private void stubEchoSave() {
        when(tenancyRepository.save(any(Tenancy.class))).thenAnswer(invocation -> {
            Tenancy t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(UUID.randomUUID());
            }
            return t;
        });
    }

    @Test
    void createTenancy_Active_MarksUnitOccupied() {
        TenancyRequest request = buildRequest(LocalDate.now().minusDays(1), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();
        stubNoOverlap();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertNotNull(response.getId());
        assertEquals(TenancyStatus.ACTIVE, response.getStatus());
        assertEquals("Wanjiru Kamau", response.getTenantName());
        assertEquals("A101", response.getUnitNumber());
        assertEquals(propertyId, response.getPropertyId());
        verify(unitService).setOccupiedStatusForTenancy(unitId, true);
    }

    @Test
    void createTenancy_InheritsRentAndDepositFromUnit() {
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();
        stubNoOverlap();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertEquals(0, new BigDecimal("45000.00").compareTo(response.getRentAmount()));
        assertEquals(0, new BigDecimal("90000.00").compareTo(response.getSecurityDepositAmount()));
    }

    @Test
    void createTenancy_ExplicitRentOverridesUnitDefault() {
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);
        request.setRentAmount(new BigDecimal("52000.00"));

        stubTenantAndUnitLookups();
        stubNoOverlap();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertEquals(0, new BigDecimal("52000.00").compareTo(response.getRentAmount()));
        // Deposit was not supplied, so it still falls back to the unit.
        assertEquals(0, new BigDecimal("90000.00").compareTo(response.getSecurityDepositAmount()));
    }

    @Test
    void createTenancy_Upcoming_LeavesUnitVacant() {
        TenancyRequest request = buildRequest(
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(13), TenancyStatus.UPCOMING);

        stubTenantAndUnitLookups();
        stubNoOverlap();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertEquals(TenancyStatus.UPCOMING, response.getStatus());
        verify(unitService, never()).setOccupiedStatusForTenancy(any(), anyBoolean());
    }

    @Test
    void createTenancy_OccupiedUnit_ThrowsBadRequest() {
        mockUnit.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("already occupied"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_UnitUnderMaintenance_ThrowsBadRequest() {
        mockUnit.setOccupancyStatus(OccupancyStatus.UNDER_MAINTENANCE);
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("under maintenance"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_OverlappingWindow_ThrowsBadRequest() {
        TenancyRequest request = buildRequest(
                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(8), TenancyStatus.UPCOMING);

        Tenancy existing = Tenancy.builder()
                .organizationId(orgId)
                .tenantId(UUID.randomUUID())
                .unitId(unitId)
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(4))
                .status(TenancyStatus.UPCOMING)
                .build();
        existing.setId(UUID.randomUUID());

        stubTenantAndUnitLookups();
        when(tenancyRepository.findOverlapping(eq(unitId), any(), any(), any(), anyCollection()))
                .thenReturn(List.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("A101"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_ActiveWithFutureStart_ThrowsBadRequest() {
        TenancyRequest request = buildRequest(LocalDate.now().plusDays(10), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("UPCOMING"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_UpcomingStartingToday_ThrowsBadRequest() {
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.UPCOMING);

        stubTenantAndUnitLookups();

        assertThrows(BadRequestException.class, () -> tenancyService.createTenancy(request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_EndBeforeStart_ThrowsBadRequest() {
        TenancyRequest request = buildRequest(
                LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(3), TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("before the start date"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_PastWithoutEndDate_ThrowsBadRequest() {
        TenancyRequest request = buildRequest(LocalDate.now().minusYears(2), null, TenancyStatus.PAST);

        stubTenantAndUnitLookups();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> tenancyService.createTenancy(request));

        assertTrue(ex.getMessage().contains("end date"));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_HistoricalRecord_SkipsOverlapAndOccupancy() {
        TenancyRequest request = buildRequest(
                LocalDate.now().minusYears(3), LocalDate.now().minusYears(2), TenancyStatus.PAST);

        stubTenantAndUnitLookups();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertEquals(TenancyStatus.PAST, response.getStatus());
        // A closed tenancy neither reserves the unit nor conflicts with live ones.
        verify(tenancyRepository, never()).findOverlapping(any(), any(), any(), any(), anyCollection());
        verify(unitService, never()).setOccupiedStatusForTenancy(any(), anyBoolean());
    }

    @Test
    void createTenancy_TenantFromAnotherOrganization_ThrowsNotFound() {
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        when(tenantRepository.findByIdAndOrganizationId(tenantId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenancyService.createTenancy(request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void createTenancy_UnitFromAnotherOrganization_ThrowsNotFound() {
        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        when(tenantRepository.findByIdAndOrganizationId(tenantId, orgId)).thenReturn(Optional.of(mockTenant));
        when(unitRepository.findByIdAndOrganizationId(unitId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenancyService.createTenancy(request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void updateTenancy_OwnOverlapIsIgnored() {
        TenancyRequest request = buildRequest(
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), TenancyStatus.ACTIVE);

        mockUnit.setOccupancyStatus(OccupancyStatus.OCCUPIED);

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        stubTenantAndUnitLookups();
        // The repository legitimately returns the tenancy being edited.
        when(tenancyRepository.findOverlapping(eq(unitId), any(), any(), any(), anyCollection()))
                .thenReturn(List.of(mockTenancy));
        stubEchoSave();

        TenancyResponse response = tenancyService.updateTenancy(tenancyId, request);

        assertEquals(LocalDate.now().plusMonths(6), response.getEndDate());
    }

    @Test
    void updateTenancy_MovingUnit_ReleasesOldAndClaimsNew() {
        UUID newUnitId = UUID.randomUUID();
        Unit newUnit = Unit.builder()
                .propertyId(propertyId)
                .unitNumber("B202")
                .type(UnitType.ONE_BEDROOM)
                .rentAmount(new BigDecimal("30000.00"))
                .depositAmount(new BigDecimal("60000.00"))
                .occupancyStatus(OccupancyStatus.VACANT)
                .status("ACTIVE")
                .build();
        newUnit.setId(newUnitId);

        TenancyRequest request = buildRequest(LocalDate.now().minusMonths(6), null, TenancyStatus.ACTIVE);
        request.setUnitId(newUnitId);

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        when(tenantRepository.findByIdAndOrganizationId(tenantId, orgId)).thenReturn(Optional.of(mockTenant));
        when(unitRepository.findByIdAndOrganizationId(newUnitId, orgId)).thenReturn(Optional.of(newUnit));
        when(tenancyRepository.findOverlapping(eq(newUnitId), any(), any(), any(), anyCollection()))
                .thenReturn(Collections.emptyList());
        stubEchoSave();

        TenancyResponse response = tenancyService.updateTenancy(tenancyId, request);

        assertEquals("B202", response.getUnitNumber());
        verify(unitService).setOccupiedStatusForTenancy(unitId, false);
        verify(unitService).setOccupiedStatusForTenancy(newUnitId, true);
    }

    @Test
    void updateTenancy_DeactivatingReleasesUnit() {
        TenancyRequest request = buildRequest(
                LocalDate.now().minusMonths(6), LocalDate.now().minusDays(1), TenancyStatus.PAST);

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        stubTenantAndUnitLookups();
        stubEchoSave();

        tenancyService.updateTenancy(tenancyId, request);

        verify(unitService).setOccupiedStatusForTenancy(unitId, false);
        verify(unitService, never()).setOccupiedStatusForTenancy(unitId, true);
    }

    @Test
    void getTenancies_FiltersByStatus() {
        when(tenancyRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(
                eq(orgId), eq(TenancyStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockTenancy)));
        when(tenantRepository.findAllById(anyCollection())).thenReturn(List.of(mockTenant));
        when(unitRepository.findAllById(anyCollection())).thenReturn(List.of(mockUnit));

        Page<TenancyResponse> result = tenancyService.getTenancies(
                TenancyStatus.ACTIVE, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Wanjiru Kamau", result.getContent().get(0).getTenantName());
        assertEquals("A101", result.getContent().get(0).getUnitNumber());
        verify(tenancyRepository, never()).findByOrganizationIdAndDeletedAtIsNull(any(), any());
    }

    @Test
    void getTenancies_FiltersByTenant() {
        when(tenancyRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(
                eq(orgId), eq(tenantId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockTenancy)));
        when(tenantRepository.findAllById(anyCollection())).thenReturn(List.of(mockTenant));
        when(unitRepository.findAllById(anyCollection())).thenReturn(List.of(mockUnit));

        Page<TenancyResponse> result = tenancyService.getTenancies(
                null, tenantId, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTenancies_ResolvesNamesInTwoQueriesForAnyPageSize() {
        Tenancy second = Tenancy.builder()
                .organizationId(orgId)
                .tenantId(tenantId)
                .unitId(unitId)
                .startDate(LocalDate.now().minusYears(2))
                .endDate(LocalDate.now().minusYears(1))
                .status(TenancyStatus.PAST)
                .build();
        second.setId(UUID.randomUUID());

        when(tenancyRepository.findByOrganizationIdAndDeletedAtIsNull(eq(orgId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockTenancy, second)));
        when(tenantRepository.findAllById(anyCollection())).thenReturn(List.of(mockTenant));
        when(unitRepository.findAllById(anyCollection())).thenReturn(List.of(mockUnit));

        Page<TenancyResponse> result = tenancyService.getTenancies(null, null, null, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        verify(tenantRepository).findAllById(anyCollection());
        verify(unitRepository).findAllById(anyCollection());
        verify(tenantRepository, never()).findById(any());
        verify(unitRepository, never()).findById(any());
    }

    @Test
    void getTenancies_TenantAndUnitFilterTogether_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> tenancyService.getTenancies(null, tenantId, unitId, PageRequest.of(0, 10)));
    }

    @Test
    void endTenancy_ClosesRecordAndReleasesUnit() {
        EndTenancyRequest request = new EndTenancyRequest();
        request.setEndDate(LocalDate.now());

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(mockTenant));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(mockUnit));
        stubEchoSave();

        TenancyResponse response = tenancyService.endTenancy(tenancyId, request);

        assertEquals(TenancyStatus.PAST, response.getStatus());
        assertEquals(LocalDate.now(), response.getEndDate());
        verify(unitService).setOccupiedStatusForTenancy(unitId, false);
    }

    @Test
    void endTenancy_AsEvicted_RecordsEviction() {
        EndTenancyRequest request = new EndTenancyRequest();
        request.setEndDate(LocalDate.now());
        request.setStatus(TenancyStatus.EVICTED);

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(mockTenant));
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(mockUnit));
        stubEchoSave();

        TenancyResponse response = tenancyService.endTenancy(tenancyId, request);

        assertEquals(TenancyStatus.EVICTED, response.getStatus());
        verify(unitService).setOccupiedStatusForTenancy(unitId, false);
    }

    @Test
    void endTenancy_AlreadyEnded_ThrowsBadRequest() {
        mockTenancy.setStatus(TenancyStatus.PAST);
        mockTenancy.setEndDate(LocalDate.now().minusDays(30));

        EndTenancyRequest request = new EndTenancyRequest();
        request.setEndDate(LocalDate.now());

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));

        assertThrows(BadRequestException.class, () -> tenancyService.endTenancy(tenancyId, request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void endTenancy_EndDateBeforeStart_ThrowsBadRequest() {
        EndTenancyRequest request = new EndTenancyRequest();
        request.setEndDate(LocalDate.now().minusYears(5));

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));

        assertThrows(BadRequestException.class, () -> tenancyService.endTenancy(tenancyId, request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void endTenancy_NonTerminalTargetStatus_ThrowsBadRequest() {
        EndTenancyRequest request = new EndTenancyRequest();
        request.setEndDate(LocalDate.now());
        request.setStatus(TenancyStatus.ACTIVE);

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));

        assertThrows(BadRequestException.class, () -> tenancyService.endTenancy(tenancyId, request));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void deleteTenancy_SoftDeletesAndReleasesUnit() {
        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        stubEchoSave();

        tenancyService.deleteTenancy(tenancyId);

        assertNotNull(mockTenancy.getDeletedAt());
        verify(unitService).setOccupiedStatusForTenancy(unitId, false);
        verify(tenancyRepository).save(mockTenancy);
        verify(tenancyRepository, never()).delete(any());
    }

    @Test
    void deleteTenancy_NonActive_DoesNotTouchOccupancy() {
        mockTenancy.setStatus(TenancyStatus.PAST);
        mockTenancy.setEndDate(LocalDate.now().minusDays(1));

        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        stubEchoSave();

        tenancyService.deleteTenancy(tenancyId);

        assertNotNull(mockTenancy.getDeletedAt());
        verify(unitService, never()).setOccupiedStatusForTenancy(any(), anyBoolean());
    }

    @Test
    void deleteTenancy_CrossOrganization_ThrowsNotFound() {
        UUID foreignId = UUID.randomUUID();
        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(foreignId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenancyService.deleteTenancy(foreignId));
        verify(tenancyRepository, never()).save(any());
    }

    @Test
    void getTenancyById_SoftDeletedIsInvisible() {
        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenancyService.getTenancyById(tenancyId));
    }

    @Test
    void getTenancyById_MissingCounterparts_StillRenders() {
        when(tenancyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(tenancyId, orgId))
                .thenReturn(Optional.of(mockTenancy));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        TenancyResponse response = tenancyService.getTenancyById(tenancyId);

        assertEquals("Unknown tenant", response.getTenantName());
        assertEquals("Unknown unit", response.getUnitNumber());
        assertNull(response.getPropertyId());
    }

    @Test
    void createTenancy_CorporateTenant_UsesCompanyName() {
        mockTenant.setTenantType(TenantType.CORPORATE);
        mockTenant.setCompanyName("Safari Holdings Ltd");

        TenancyRequest request = buildRequest(LocalDate.now(), null, TenancyStatus.ACTIVE);

        stubTenantAndUnitLookups();
        stubNoOverlap();
        stubEchoSave();

        TenancyResponse response = tenancyService.createTenancy(request);

        assertEquals("Safari Holdings Ltd", response.getTenantName());
    }
}
