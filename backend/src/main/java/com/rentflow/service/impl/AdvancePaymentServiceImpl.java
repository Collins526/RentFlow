package com.rentflow.service.impl;

import com.rentflow.dto.request.AdvancePaymentRequest;
import com.rentflow.dto.response.AdvancePaymentResponse;
import com.rentflow.entity.AdvancePayment;
import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.RentLedgerEntry;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.enums.AdvanceStatus;
import com.rentflow.entity.enums.LedgerEntryType;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.AdvancePaymentMapper;
import com.rentflow.repository.AdvancePaymentRepository;
import com.rentflow.repository.RentInvoiceRepository;
import com.rentflow.repository.RentLedgerRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.AdvancePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvancePaymentServiceImpl implements AdvancePaymentService {

    private final AdvancePaymentRepository advancePaymentRepository;
    private final TenantRepository tenantRepository;
    private final RentInvoiceRepository rentInvoiceRepository;
    private final RentLedgerRepository rentLedgerRepository;

    @Override
    @Transactional
    public AdvancePaymentResponse receiveAdvance(AdvancePaymentRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        AdvancePayment a = AdvancePayment.builder()
                .organizationId(organizationId)
                .tenantId(tenant.getId())
                .leaseId(request.getLeaseId())
                .amount(request.getAmount())
                .remainingAmount(request.getAmount())
                .status(AdvanceStatus.AVAILABLE)
                .note(request.getNote())
                .receivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now())
                .build();

        AdvancePayment saved = advancePaymentRepository.save(a);
        return AdvancePaymentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public AdvancePaymentResponse allocateAdvance(UUID advanceId, UUID invoiceId, java.math.BigDecimal amount) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Allocation amount must be greater than zero");
        }

        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        AdvancePayment advancePayment = advancePaymentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(advanceId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance payment not found"));

        if (advancePayment.getRemainingAmount().compareTo(amount) < 0) {
            throw new BadRequestException("Allocation amount exceeds available advance balance");
        }

        RentInvoice invoice = rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(invoiceId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getAmount() == null) {
            throw new BadRequestException("Invoice amount must be defined before applying an advance");
        }

        if (invoice.getStatus() == com.rentflow.entity.enums.InvoiceStatus.PAID || invoice.getStatus() == com.rentflow.entity.enums.InvoiceStatus.VOID) {
            throw new BadRequestException("Cannot allocate advance to a paid or void invoice");
        }

        if (amount.compareTo(invoice.getAmount()) > 0) {
            throw new BadRequestException("Allocation amount cannot exceed invoice amount");
        }

        advancePayment.setRemainingAmount(advancePayment.getRemainingAmount().subtract(amount));
        advancePayment.setStatus(advancePayment.getRemainingAmount().compareTo(java.math.BigDecimal.ZERO) == 0
                ? AdvanceStatus.APPLIED
                : AdvanceStatus.PARTIALLY_APPLIED);

        if (amount.compareTo(invoice.getAmount()) == 0) {
            invoice.setStatus(com.rentflow.entity.enums.InvoiceStatus.PAID);
            rentInvoiceRepository.save(invoice);
        }

        RentLedgerEntry ledgerEntry = RentLedgerEntry.builder()
                .organizationId(organizationId)
                .tenancyId(invoice.getTenancyId())
                .leaseId(advancePayment.getLeaseId())
                .invoiceId(invoice.getId())
                .tenantId(advancePayment.getTenantId())
                .unitId(invoice.getUnitId())
                .entryDate(java.time.LocalDate.now())
                .type(LedgerEntryType.PAYMENT)
                .description("Advance payment applied to invoice")
                .amount(amount)
                .build();

        rentLedgerRepository.save(ledgerEntry);
        AdvancePayment saved = advancePaymentRepository.save(advancePayment);
        return AdvancePaymentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvancePaymentResponse getAdvanceById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        AdvancePayment a = advancePaymentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance payment not found"));
        return AdvancePaymentMapper.toDto(a);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvancePaymentResponse> listAdvances(UUID tenantId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Page<AdvancePayment> page = tenantId != null
                ? advancePaymentRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable)
                : advancePaymentRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);

        return page.map(AdvancePaymentMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteAdvance(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        AdvancePayment a = advancePaymentRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance payment not found"));

        a.setDeletedAt(Instant.now());
        advancePaymentRepository.save(a);
    }
}
