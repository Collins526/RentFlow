package com.rentflow.service.impl;

import com.rentflow.dto.request.RentInvoiceRequest;
import com.rentflow.dto.response.RentInvoiceResponse;
import com.rentflow.entity.RentInvoice;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.InvoiceStatus;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.RentInvoiceMapper;
import com.rentflow.repository.RentInvoiceRepository;
import com.rentflow.repository.PaymentRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.RentBillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentBillingServiceImpl implements RentBillingService {

    private final RentInvoiceRepository rentInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public RentInvoiceResponse createInvoice(RentInvoiceRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Unit unit = unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BadRequestException("Period end cannot be before period start");
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : resolveDefaultAmount(unit);

        RentInvoice invoice = RentInvoice.builder()
                .organizationId(organizationId)
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .tenantId(tenant.getId())
                .unitId(unit.getId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : request.getPeriodEnd())
                .amount(amount)
                .status(InvoiceStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        RentInvoice saved = rentInvoiceRepository.save(invoice);
        return RentInvoiceMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public RentInvoiceResponse issueInvoice(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentInvoice invoice = getInvoiceOrThrow(id, organizationId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BadRequestException("Only draft invoices can be issued");
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        RentInvoice saved = rentInvoiceRepository.save(invoice);

        Tenant tenant = tenantRepository.findById(saved.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(saved.getUnitId()).orElse(null);

        return RentInvoiceMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional
    public RentInvoiceResponse markPaid(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentInvoice invoice = getInvoiceOrThrow(id, organizationId);

        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new BadRequestException("Only issued invoices can be marked paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        RentInvoice saved = rentInvoiceRepository.save(invoice);

        Tenant tenant = tenantRepository.findById(saved.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(saved.getUnitId()).orElse(null);

        return RentInvoiceMapper.toDto(saved, tenant, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public RentInvoiceResponse getInvoiceById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentInvoice invoice = getInvoiceOrThrow(id, organizationId);

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null || !currentTenantId.equals(invoice.getTenantId())) {
                throw new UnauthorizedException("Tenant can only access their own invoices");
            }
        }

        settleInvoiceFromCompletedPayment(invoice);
        Tenant tenant = tenantRepository.findById(invoice.getTenantId()).orElse(null);
        Unit unit = unitRepository.findById(invoice.getUnitId()).orElse(null);

        return RentInvoiceMapper.toDto(invoice, tenant, unit);
    }

    @Override
    @Transactional
    public Page<RentInvoiceResponse> getInvoices(com.rentflow.entity.enums.InvoiceStatus status, UUID tenantId, UUID unitId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        if (SecurityUtils.hasRole("TENANT")) {
            UUID currentTenantId = SecurityUtils.getCurrentUserTenantIdOrNull();
            if (currentTenantId == null) {
                throw new UnauthorizedException("Tenant is not linked to a tenant record");
            }
            if (tenantId != null && !currentTenantId.equals(tenantId)) {
                throw new UnauthorizedException("Tenant can only view their own invoice history");
            }
            tenantId = currentTenantId;
            unitId = null;
        }

        Page<com.rentflow.entity.RentInvoice> page;
        if (tenantId != null) {
            page = rentInvoiceRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (unitId != null) {
            page = rentInvoiceRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else if (status != null) {
            page = rentInvoiceRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, status, pageable);
        } else {
            page = rentInvoiceRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(inv -> {
            settleInvoiceFromCompletedPayment(inv);
            return RentInvoiceMapper.toDto(inv,
                tenantRepository.findById(inv.getTenantId()).orElse(null),
            unitRepository.findById(inv.getUnitId()).orElse(null));
        });
    }

    @Override
    @Transactional
    public void deleteInvoice(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentInvoice invoice = getInvoiceOrThrow(id, organizationId);

        invoice.setDeletedAt(Instant.now());
        rentInvoiceRepository.save(invoice);
    }

    private BigDecimal resolveDefaultAmount(Unit unit) {
        return unit.getRentAmount() != null ? unit.getRentAmount() : BigDecimal.ZERO;
    }

    private RentInvoice getInvoiceOrThrow(UUID id, UUID organizationId) {
        return rentInvoiceRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    private void settleInvoiceFromCompletedPayment(RentInvoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.PAID
                || !paymentRepository.existsByInvoiceIdAndStatusAndDeletedAtIsNull(invoice.getId(),
                com.rentflow.entity.enums.PaymentStatus.COMPLETED)) {
            return;
        }

        invoice.setStatus(InvoiceStatus.PAID);
        rentInvoiceRepository.save(invoice);
    }
}
