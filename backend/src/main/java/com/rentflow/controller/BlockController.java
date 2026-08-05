package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.BlockRequest;
import com.rentflow.dto.response.BlockResponse;
import com.rentflow.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/api/v1/properties/{propertyId}/blocks")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')")
    public ResponseEntity<ApiResponse<BlockResponse>> createBlock(
            @PathVariable UUID propertyId, @Valid @RequestBody BlockRequest request) {
        BlockResponse block = blockService.createBlock(propertyId, request);
        return new ResponseEntity<>(ApiResponse.success(block, "Block created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/properties/{propertyId}/blocks")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Page<BlockResponse>>> getBlocksByProperty(
            @PathVariable UUID propertyId, Pageable pageable) {
        Page<BlockResponse> blocks = blockService.getBlocksByPropertyId(propertyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(blocks, "Blocks fetched successfully"));
    }

    @GetMapping("/api/v1/blocks/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<BlockResponse>> getBlockById(@PathVariable UUID id) {
        BlockResponse block = blockService.getBlockById(id);
        return ResponseEntity.ok(ApiResponse.success(block, "Block fetched successfully"));
    }

    @PutMapping("/api/v1/blocks/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER') or hasRole('PROPERTY_MANAGER')")
    public ResponseEntity<ApiResponse<BlockResponse>> updateBlock(
            @PathVariable UUID id, @Valid @RequestBody BlockRequest request) {
        BlockResponse block = blockService.updateBlock(id, request);
        return ResponseEntity.ok(ApiResponse.success(block, "Block updated successfully"));
    }

    @DeleteMapping("/api/v1/blocks/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(@PathVariable UUID id) {
        blockService.deleteBlock(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Block deleted successfully"));
    }
}
