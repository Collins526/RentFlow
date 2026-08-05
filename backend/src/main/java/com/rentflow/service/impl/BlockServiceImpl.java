package com.rentflow.service.impl;

import com.rentflow.dto.request.BlockRequest;
import com.rentflow.dto.response.BlockResponse;
import com.rentflow.entity.Block;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.mapper.BlockMapper;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public BlockResponse createBlock(UUID propertyId, BlockRequest request) {
        UUID organizationId = getCurrentUserOrganizationId();

        // Verify the property exists AND belongs to this user's organization
        propertyRepository.findByIdAndOrganizationId(propertyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        Block block = BlockMapper.toEntity(request, propertyId);
        Block savedBlock = blockRepository.save(block);
        return BlockMapper.toDto(savedBlock);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlockResponse> getBlocksByPropertyId(UUID propertyId, Pageable pageable) {
        UUID organizationId = getCurrentUserOrganizationId();

        // Verify property ownership before listing blocks
        propertyRepository.findByIdAndOrganizationId(propertyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        return blockRepository.findByPropertyId(propertyId, pageable)
                .map(BlockMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse getBlockById(UUID id) {
        Block block = getBlockEntity(id);
        return BlockMapper.toDto(block);
    }

    @Override
    @Transactional
    public BlockResponse updateBlock(UUID id, BlockRequest request) {
        Block block = getBlockEntity(id);

        block.setName(request.getName());
        block.setDescription(request.getDescription());
        if (request.getNumberOfFloors() != null) {
            block.setNumberOfFloors(request.getNumberOfFloors());
        }

        Block updatedBlock = blockRepository.save(block);
        return BlockMapper.toDto(updatedBlock);
    }

    @Override
    @Transactional
    public void deleteBlock(UUID id) {
        Block block = getBlockEntity(id);
        blockRepository.delete(block);
    }

    /**
     * Retrieves a Block entity while verifying that it belongs to the current user's organization
     * via its parent Property. This is the cascading multi-tenancy check.
     */
    private Block getBlockEntity(UUID id) {
        UUID organizationId = getCurrentUserOrganizationId();
        return blockRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + id));
    }

    private UUID getCurrentUserOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UUID orgId = userDetails.getUser().getOrganizationId();

        if (orgId == null) {
            throw new ResourceNotFoundException("User does not belong to any organization");
        }

        return orgId;
    }
}
