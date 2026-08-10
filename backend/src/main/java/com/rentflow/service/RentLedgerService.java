package com.rentflow.service;

import com.rentflow.dto.request.RentLedgerRequest;
import com.rentflow.dto.response.RentLedgerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface RentLedgerService {

    RentLedgerResponse createEntry(RentLedgerRequest request);

    RentLedgerResponse getEntryById(UUID id);

    Page<RentLedgerResponse> getEntries(UUID tenantId, UUID unitId, LocalDate start, LocalDate end, Pageable pageable);

    void deleteEntry(UUID id);
}
