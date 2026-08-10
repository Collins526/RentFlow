package com.rentflow.service;

import com.rentflow.dto.request.RentInvoiceRequest;
import com.rentflow.dto.response.RentInvoiceResponse;
import com.rentflow.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RentBillingService {

    RentInvoiceResponse createInvoice(RentInvoiceRequest request);

    RentInvoiceResponse issueInvoice(UUID id);

    RentInvoiceResponse markPaid(UUID id);

    RentInvoiceResponse getInvoiceById(UUID id);

    Page<RentInvoiceResponse> getInvoices(InvoiceStatus status, UUID tenantId, UUID unitId, Pageable pageable);

    void deleteInvoice(UUID id);
}
