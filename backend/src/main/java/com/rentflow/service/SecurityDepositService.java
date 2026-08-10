package com.rentflow.service;

import com.rentflow.dto.request.SecurityDepositRefundRequest;
import com.rentflow.dto.request.SecurityDepositRequest;
import com.rentflow.dto.response.SecurityDepositResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SecurityDepositService {

    SecurityDepositResponse createDeposit(SecurityDepositRequest request);

    SecurityDepositResponse refundDeposit(UUID id, SecurityDepositRefundRequest request);

    SecurityDepositResponse getDepositById(UUID id);

    Page<SecurityDepositResponse> listDeposits(UUID tenantId, UUID tenancyId, Pageable pageable);

    void deleteDeposit(UUID id);
}
