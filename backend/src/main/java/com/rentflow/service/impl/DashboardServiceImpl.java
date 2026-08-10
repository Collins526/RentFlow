package com.rentflow.service.impl;

import com.rentflow.dto.response.DashboardSummaryResponse;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.TenancyRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PropertyRepository propertyRepository;
    private final BlockRepository blockRepository;
    private final UnitRepository unitRepository;
    private final TenantRepository tenantRepository;
    private final TenancyRepository tenancyRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Map<OccupancyStatus, Long> unitCounts = new EnumMap<>(OccupancyStatus.class);
        BigDecimal potentialMonthlyRent = BigDecimal.ZERO;
        long totalUnits = 0L;

        // One grouped query covers every occupancy bucket and the rent roll at once.
        for (Object[] row : unitRepository.countByOccupancyStatusForOrganization(organizationId)) {
            OccupancyStatus status = (OccupancyStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal rent = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            unitCounts.put(status, count);
            totalUnits += count;
            potentialMonthlyRent = potentialMonthlyRent.add(rent);
        }

        long occupiedUnits = unitCounts.getOrDefault(OccupancyStatus.OCCUPIED, 0L);

        return DashboardSummaryResponse.builder()
                .totalProperties(propertyRepository.countByOrganizationId(organizationId))
                .totalBlocks(blockRepository.countByOrganizationId(organizationId))
                .totalUnits(totalUnits)
                .occupiedUnits(occupiedUnits)
                .vacantUnits(unitCounts.getOrDefault(OccupancyStatus.VACANT, 0L))
                .reservedUnits(unitCounts.getOrDefault(OccupancyStatus.RESERVED, 0L))
                .unitsUnderMaintenance(unitCounts.getOrDefault(OccupancyStatus.UNDER_MAINTENANCE, 0L))
                .occupancyRate(occupancyRate(occupiedUnits, totalUnits))
                .totalTenants(tenantRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId))
                .activeTenancies(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(
                        organizationId, TenancyStatus.ACTIVE))
                .upcomingTenancies(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(
                        organizationId, TenancyStatus.UPCOMING))
                .contractedMonthlyRent(tenancyRepository.sumRentByStatus(organizationId, TenancyStatus.ACTIVE))
                .potentialMonthlyRent(potentialMonthlyRent)
                .build();
    }

    /** Guards the empty-portfolio case, where a percentage of zero units is undefined. */
    private BigDecimal occupancyRate(long occupied, long total) {
        if (total == 0L) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(occupied)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
