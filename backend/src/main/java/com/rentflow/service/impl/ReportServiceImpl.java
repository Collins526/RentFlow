package com.rentflow.service.impl;

import com.rentflow.dto.response.ReportSummaryResponse;
import com.rentflow.entity.enums.InvoiceStatus;
import com.rentflow.entity.enums.OccupancyStatus;
import com.rentflow.entity.enums.PaymentStatus;
import com.rentflow.entity.enums.TenancyStatus;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PaymentRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.RentInvoiceRepository;
import com.rentflow.repository.TenancyRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final PropertyRepository propertyRepository;
    private final BlockRepository blockRepository;
    private final UnitRepository unitRepository;
    private final TenantRepository tenantRepository;
    private final TenancyRepository tenancyRepository;
    private final RentInvoiceRepository rentInvoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummaryReport() {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Map<OccupancyStatus, Long> unitCounts = new EnumMap<>(OccupancyStatus.class);
        BigDecimal potentialMonthlyRent = BigDecimal.ZERO;
        long totalUnits = 0L;

        for (Object[] row : unitRepository.countByOccupancyStatusForOrganization(organizationId)) {
            OccupancyStatus status = (OccupancyStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal rent = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            unitCounts.put(status, count);
            totalUnits += count;
            potentialMonthlyRent = potentialMonthlyRent.add(rent);
        }

        long occupiedUnits = unitCounts.getOrDefault(OccupancyStatus.OCCUPIED, 0L);
        BigDecimal occupancyRate = calculateOccupancyRate(occupiedUnits, totalUnits);

        long totalInvoices = rentInvoiceRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId);
        long issuedInvoices = rentInvoiceRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, InvoiceStatus.ISSUED);
        long paidInvoices = rentInvoiceRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, InvoiceStatus.PAID);
        long overdueInvoices = rentInvoiceRepository.countByOrganizationIdAndDueDateBeforeAndStatusNotAndDeletedAtIsNull(
                organizationId, LocalDate.now(), InvoiceStatus.PAID);
        BigDecimal totalInvoiceAmount = rentInvoiceRepository.sumAmountByOrganizationId(organizationId);
        BigDecimal totalPaidAmount = rentInvoiceRepository.sumAmountByOrganizationIdAndStatus(organizationId, InvoiceStatus.PAID);
        BigDecimal totalOutstandingAmount = totalInvoiceAmount.subtract(totalPaidAmount);

        long totalPayments = paymentRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId);
        long completedPayments = paymentRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, PaymentStatus.COMPLETED);
        long failedPayments = paymentRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, PaymentStatus.FAILED);
        BigDecimal totalPaymentAmount = paymentRepository.sumAmountByOrganizationId(organizationId);

        return ReportSummaryResponse.builder()
                .totalProperties(propertyRepository.countByOrganizationId(organizationId))
                .totalBlocks(blockRepository.countByOrganizationId(organizationId))
                .totalUnits(totalUnits)
                .occupiedUnits(occupiedUnits)
                .vacantUnits(unitCounts.getOrDefault(OccupancyStatus.VACANT, 0L))
                .reservedUnits(unitCounts.getOrDefault(OccupancyStatus.RESERVED, 0L))
                .unitsUnderMaintenance(unitCounts.getOrDefault(OccupancyStatus.UNDER_MAINTENANCE, 0L))
                .occupancyRate(occupancyRate)
                .totalTenants(tenantRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId))
                .activeTenancies(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, TenancyStatus.ACTIVE))
                .upcomingTenancies(tenancyRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, TenancyStatus.UPCOMING))
                .contractedMonthlyRent(tenancyRepository.sumRentByStatus(organizationId, TenancyStatus.ACTIVE))
                .potentialMonthlyRent(potentialMonthlyRent)
                .totalInvoices(totalInvoices)
                .issuedInvoices(issuedInvoices)
                .paidInvoices(paidInvoices)
                .overdueInvoices(overdueInvoices)
                .totalInvoiceAmount(totalInvoiceAmount)
                .totalPaidAmount(totalPaidAmount)
                .totalOutstandingAmount(totalOutstandingAmount)
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .totalPaymentAmount(totalPaymentAmount)
                .build();
    }

    private BigDecimal calculateOccupancyRate(long occupiedUnits, long totalUnits) {
        if (totalUnits == 0L) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(occupiedUnits)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalUnits), 1, RoundingMode.HALF_UP);
    }
}
