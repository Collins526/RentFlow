package com.rentflow.service.impl;

import com.rentflow.dto.request.RentLedgerRequest;
import com.rentflow.dto.response.RentLedgerResponse;
import com.rentflow.entity.RentLedgerEntry;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.RentLedgerMapper;
import com.rentflow.repository.RentLedgerRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.RentLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentLedgerServiceImpl implements RentLedgerService {

    private final RentLedgerRepository rentLedgerRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public RentLedgerResponse createEntry(RentLedgerRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        RentLedgerEntry entry = RentLedgerEntry.builder()
                .organizationId(organizationId)
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .invoiceId(request.getInvoiceId())
                .paymentId(request.getPaymentId())
                .tenantId(tenant.getId())
                .unitId(unit != null ? unit.getId() : null)
                .entryDate(request.getEntryDate())
                .type(request.getType())
                .description(request.getDescription())
                .amount(request.getAmount())
                .build();

        RentLedgerEntry saved = rentLedgerRepository.save(entry);
        return RentLedgerMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RentLedgerResponse getEntryById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentLedgerEntry entry = rentLedgerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found"));

        return RentLedgerMapper.toDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentLedgerResponse> getEntries(UUID tenantId, UUID unitId, LocalDate start, LocalDate end, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Page<RentLedgerEntry> page;
        if (tenantId != null) {
            page = rentLedgerRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (unitId != null) {
            page = rentLedgerRepository.findByOrganizationIdAndUnitIdAndDeletedAtIsNull(organizationId, unitId, pageable);
        } else if (start != null && end != null) {
            page = rentLedgerRepository.findByOrganizationIdAndEntryDateBetweenAndDeletedAtIsNull(organizationId, start, end, pageable);
        } else {
            page = rentLedgerRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(RentLedgerMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteEntry(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        RentLedgerEntry entry = rentLedgerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found"));

        entry.setDeletedAt(Instant.now());
        rentLedgerRepository.save(entry);
    }
}
