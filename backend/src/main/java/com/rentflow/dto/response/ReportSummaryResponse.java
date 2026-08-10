package com.rentflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryResponse {
    private long totalProperties;
    private long totalBlocks;
    private long totalUnits;
    private long occupiedUnits;
    private long vacantUnits;
    private long reservedUnits;
    private long unitsUnderMaintenance;
    private BigDecimal occupancyRate;
    private long totalTenants;
    private long activeTenancies;
    private long upcomingTenancies;
    private BigDecimal contractedMonthlyRent;
    private BigDecimal potentialMonthlyRent;
    private long totalInvoices;
    private long issuedInvoices;
    private long paidInvoices;
    private long overdueInvoices;
    private BigDecimal totalInvoiceAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalOutstandingAmount;
    private long totalPayments;
    private long completedPayments;
    private long failedPayments;
    private BigDecimal totalPaymentAmount;
}
