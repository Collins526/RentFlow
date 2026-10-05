package com.rentflow.service;

import com.rentflow.dto.request.SalesInquiryRequest;
import com.rentflow.dto.response.SalesInquiryResponse;
import com.rentflow.entity.SalesInquiry;
import com.rentflow.repository.SalesInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalesInquiryService {

    private final SalesInquiryRepository repository;

    public SalesInquiryResponse submit(SalesInquiryRequest request) {
        SalesInquiry inquiry = new SalesInquiry();
        inquiry.setName(request.name().trim());
        inquiry.setEmail(request.email().trim());
        inquiry.setMessage(request.message().trim());
        return SalesInquiryResponse.from(repository.save(inquiry));
    }

    public Page<SalesInquiryResponse> list(Pageable pageable) {
        return repository.findAllByDeletedAtIsNull(pageable).map(SalesInquiryResponse::from);
    }
}