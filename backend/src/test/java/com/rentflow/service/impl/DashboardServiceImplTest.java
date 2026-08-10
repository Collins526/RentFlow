package com.rentflow.service.impl;

import com.rentflow.dto.response.DashboardSummaryResponse;
import com.rentflow.entity.User;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.TenancyRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.exception.UnauthorizedException;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenancyRepository tenancyRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        User user = User.builder()
                .email("owner@testorg.com")
                .organizationId(orgId)
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

    private void stubCounts(long properties, long blocks, long tenants, long active, long upcoming,
                            BigDecimal contractedRent) {
        lenient().when(propertyRepository.countByOrganizationId(orgId)).thenReturn(properties);
        lenient().when(blockRepository.countByOrganizationId(orgId)).thenReturn(blocks);
        lenient().when(tenantRepository.countByOrganizationIdAndDeletedAtIsNull(orgId)).thenReturn(tenants);
        lenient().when(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(
                orgId, TenancyStatus.ACTIVE)).thenReturn(active);
        lenient().when(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(
                orgId, TenancyStatus.UPCOMING)).thenReturn(upcoming);
        lenient().when(tenancyRepository.sumRentByStatus(orgId, TenancyStatus.ACTIVE))
                .thenReturn(contractedRent);
    }

    @Test
    void getSummary_AggregatesEveryOccupancyBucket() {
        stubCounts(4L, 6L, 30L, 21L, 3L, new BigDecimal("945000.00"));
        when(unitRepository.countByOccupancyStatusForOrganization(orgId)).thenReturn(List.of(
                new Object[]{OccupancyStatus.OCCUPIED, 21L, new BigDecimal("945000.00")},
                new Object[]{OccupancyStatus.VACANT, 6L, new BigDecimal("270000.00")},
                new Object[]{OccupancyStatus.RESERVED, 2L, new BigDecimal("90000.00")},
                new Object[]{OccupancyStatus.UNDER_MAINTENANCE, 1L, new BigDecimal("45000.00")}
        ));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertEquals(4L, summary.getTotalProperties());
        assertEquals(6L, summary.getTotalBlocks());
        assertEquals(30L, summary.getTotalUnits());
        assertEquals(21L, summary.getOccupiedUnits());
        assertEquals(6L, summary.getVacantUnits());
        assertEquals(2L, summary.getReservedUnits());
        assertEquals(1L, summary.getUnitsUnderMaintenance());
        assertEquals(30L, summary.getTotalTenants());
        assertEquals(21L, summary.getActiveTenancies());
        assertEquals(3L, summary.getUpcomingTenancies());
        assertEquals(0, new BigDecimal("945000.00").compareTo(summary.getContractedMonthlyRent()));
        assertEquals(0, new BigDecimal("1350000.00").compareTo(summary.getPotentialMonthlyRent()));
    }

    @Test
    void getSummary_OccupancyRateRoundsToOneDecimal() {
        stubCounts(1L, 0L, 0L, 1L, 0L, BigDecimal.ZERO);
        when(unitRepository.countByOccupancyStatusForOrganization(orgId)).thenReturn(List.of(
                new Object[]{OccupancyStatus.OCCUPIED, 1L, new BigDecimal("30000.00")},
                new Object[]{OccupancyStatus.VACANT, 2L, new BigDecimal("60000.00")}
        ));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        // 1 of 3 -> 33.333... -> 33.3
        assertEquals(0, new BigDecimal("33.3").compareTo(summary.getOccupancyRate()));
    }

    @Test
    void getSummary_EmptyPortfolio_ReturnsZeroesNotDivideByZero() {
        stubCounts(0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO);
        when(unitRepository.countByOccupancyStatusForOrganization(orgId))
                .thenReturn(Collections.emptyList());

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertEquals(0L, summary.getTotalUnits());
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getOccupancyRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getPotentialMonthlyRent()));
    }

    @Test
    void getSummary_FullyOccupied_ReportsHundredPercent() {
        stubCounts(1L, 0L, 5L, 5L, 0L, new BigDecimal("150000.00"));
        when(unitRepository.countByOccupancyStatusForOrganization(orgId)).thenReturn(List.<Object[]>of(
                new Object[]{OccupancyStatus.OCCUPIED, 5L, new BigDecimal("150000.00")}
        ));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertEquals(0, new BigDecimal("100.0").compareTo(summary.getOccupancyRate()));
    }

    @Test
    void getSummary_Unauthenticated_ThrowsUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, () -> dashboardService.getSummary());
    }
}
