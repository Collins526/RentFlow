package com.rentflow.service;

import com.rentflow.dto.request.BlockRequest;
import com.rentflow.dto.response.BlockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BlockService {
    BlockResponse createBlock(UUID propertyId, BlockRequest request);
    Page<BlockResponse> getBlocksByPropertyId(UUID propertyId, Pageable pageable);
    BlockResponse getBlockById(UUID id);
    BlockResponse updateBlock(UUID id, BlockRequest request);
    void deleteBlock(UUID id);
}
