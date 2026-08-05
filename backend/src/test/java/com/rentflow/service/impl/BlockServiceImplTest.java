package com.rentflow.service.impl;

import com.rentflow.dto.request.BlockRequest;
import com.rentflow.dto.response.BlockResponse;
import com.rentflow.entity.Block;
import com.rentflow.entity.Property;
import com.rentflow.entity.User;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.repository.BlockRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private BlockServiceImpl blockService;

    private UUID orgId;
    private UUID propertyId;
    private UUID blockId;
    private Property mockProperty;
    private Block mockBlock;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        blockId = UUID.randomUUID();

        mockProperty = Property.builder()
                .organizationId(orgId)
                .name("Test Property")
                .type("RESIDENTIAL")
                .address("123 Test St")
                .status("ACTIVE")
                .build();
        mockProperty.setId(propertyId);

        mockBlock = Block.builder()
                .propertyId(propertyId)
                .name("Building A")
                .numberOfFloors(5)
                .build();
        mockBlock.setId(blockId);

        User mockUser = User.builder()
                .email("owner@testorg.com")
                .organizationId(orgId)
                .roles(new HashSet<>())
                .build();
        mockUser.setId(UUID.randomUUID());

        UserDetailsImpl userDetails = new UserDetailsImpl(mockUser, new HashSet<>());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void createBlock_Success() {
        BlockRequest request = new BlockRequest();
        request.setName("Building B");
        request.setNumberOfFloors(3);

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));
        when(blockRepository.save(any(Block.class))).thenAnswer(i -> {
            Block b = (Block) i.getArguments()[0];
            b.setId(UUID.randomUUID());
            return b;
        });

        BlockResponse response = blockService.createBlock(propertyId, request);

        assertNotNull(response);
        assertEquals("Building B", response.getName());
        assertEquals(3, response.getNumberOfFloors());
        assertEquals(propertyId, response.getPropertyId());

        verify(propertyRepository).findByIdAndOrganizationId(propertyId, orgId);
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void createBlock_PropertyNotFound_ThrowsException() {
        BlockRequest request = new BlockRequest();
        request.setName("Building C");

        UUID fakePropertyId = UUID.randomUUID();
        when(propertyRepository.findByIdAndOrganizationId(fakePropertyId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                blockService.createBlock(fakePropertyId, request));

        verify(blockRepository, never()).save(any());
    }

    @Test
    void getBlocksByPropertyId_Success() {
        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId))
                .thenReturn(Optional.of(mockProperty));

        Page<Block> blockPage = new PageImpl<>(Collections.singletonList(mockBlock));
        when(blockRepository.findByPropertyId(eq(propertyId), any(PageRequest.class)))
                .thenReturn(blockPage);

        Page<BlockResponse> result = blockService.getBlocksByPropertyId(propertyId, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Building A", result.getContent().get(0).getName());
    }

    @Test
    void getBlockById_Success() {
        when(blockRepository.findByIdAndOrganizationId(blockId, orgId))
                .thenReturn(Optional.of(mockBlock));

        BlockResponse response = blockService.getBlockById(blockId);

        assertNotNull(response);
        assertEquals("Building A", response.getName());
        assertEquals(5, response.getNumberOfFloors());
    }

    @Test
    void updateBlock_Success() {
        BlockRequest request = new BlockRequest();
        request.setName("Building A - Renovated");
        request.setNumberOfFloors(6);

        when(blockRepository.findByIdAndOrganizationId(blockId, orgId))
                .thenReturn(Optional.of(mockBlock));
        when(blockRepository.save(any(Block.class))).thenAnswer(i -> i.getArguments()[0]);

        BlockResponse response = blockService.updateBlock(blockId, request);

        assertNotNull(response);
        assertEquals("Building A - Renovated", response.getName());
        assertEquals(6, response.getNumberOfFloors());
        verify(blockRepository).save(any(Block.class));
    }
}
