package com.rentflow.service.impl;

import com.rentflow.dto.request.SecurityDepositRefundRequest;
import com.rentflow.dto.request.SecurityDepositRequest;
import com.rentflow.dto.response.SecurityDepositResponse;
import com.rentflow.entity.RentLedgerEntry;
import com.rentflow.entity.SecurityDeposit;
import com.rentflow.entity.Tenant;
import com.rentflow.entity.Unit;
import com.rentflow.entity.enums.DepositStatus;
import com.rentflow.entity.enums.LedgerEntryType;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.mapper.SecurityDepositMapper;
import com.rentflow.repository.SecurityDepositRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.repository.RentLedgerRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.SecurityDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityDepositServiceImpl implements SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final RentLedgerRepository rentLedgerRepository;

    @Override
    @Transactional
    public SecurityDepositResponse createDeposit(SecurityDepositRequest request) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Tenant tenant = tenantRepository.findByIdAndOrganizationId(request.getTenantId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findByIdAndOrganizationId(request.getUnitId(), organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
        }

        SecurityDeposit deposit = SecurityDeposit.builder()
                .organizationId(organizationId)
                .tenantId(tenant.getId())
                .tenancyId(request.getTenancyId())
                .leaseId(request.getLeaseId())
                .unitId(unit != null ? unit.getId() : null)
                .amount(request.getAmount())
                .remainingAmount(request.getAmount())
                .status(DepositStatus.HELD)
                .note(request.getNote())
                .receivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now())
                .build();

        SecurityDeposit saved = securityDepositRepository.save(deposit);
        return SecurityDepositMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SecurityDepositResponse refundDeposit(UUID id, SecurityDepositRefundRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Refund amount must be greater than zero");
        }

        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        SecurityDeposit deposit = securityDepositRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Security deposit not found"));

        if (deposit.getRemainingAmount().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Refund amount exceeds available deposit balance");
        }

        deposit.setRemainingAmount(deposit.getRemainingAmount().subtract(request.getAmount()));
        deposit.setStatus(deposit.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0
                ? DepositStatus.REFUNDED
                : DepositStatus.PARTIALLY_REFUNDED);

        RentLedgerEntry refundEntry = RentLedgerEntry.builder()
                .organizationId(organizationId)
                .tenancyId(deposit.getTenancyId())
                .leaseId(deposit.getLeaseId())
                .tenantId(deposit.getTenantId())
                .unitId(deposit.getUnitId())
                .entryDate(request.getRefundDate() != null ? request.getRefundDate() : LocalDate.now())
                .type(LedgerEntryType.ADJUSTMENT)
                .description("Security deposit refunded" +
                        (request.getNote() != null ? ": " + request.getNote() : ""))
                .amount(request.getAmount().negate())
                .build();

        rentLedgerRepository.save(refundEntry);
        SecurityDeposit saved = securityDepositRepository.save(deposit);
        return SecurityDepositMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityDepositResponse getDepositById(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        SecurityDeposit deposit = securityDepositRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Security deposit not found"));
        return SecurityDepositMapper.toDto(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityDepositResponse> listDeposits(UUID tenantId, UUID tenancyId, Pageable pageable) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();

        Page<SecurityDeposit> page;
        if (tenantId != null) {
            page = securityDepositRepository.findByOrganizationIdAndTenantIdAndDeletedAtIsNull(organizationId, tenantId, pageable);
        } else if (tenancyId != null) {
            page = securityDepositRepository.findByOrganizationIdAndTenancyIdAndDeletedAtIsNull(organizationId, tenancyId, pageable);
        } else {
            page = securityDepositRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable);
        }

        return page.map(SecurityDepositMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteDeposit(UUID id) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        SecurityDeposit deposit = securityDepositRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Security deposit not found"));

        deposit.setDeletedAt(Instant.now());
        securityDepositRepository.save(deposit);
    }
}
