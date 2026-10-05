package com.rentflow.repository;

import com.rentflow.entity.SalesInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesInquiryRepository extends JpaRepository<SalesInquiry, UUID> {
    Page<SalesInquiry> findAllByDeletedAtIsNull(Pageable pageable);
}