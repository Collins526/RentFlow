package com.rentflow.mapper;

import com.rentflow.dto.request.BlockRequest;
import com.rentflow.dto.response.BlockResponse;
import com.rentflow.entity.Block;

import java.util.UUID;

public class BlockMapper {

    public static BlockResponse toDto(Block block) {
        if (block == null) {
            return null;
        }

        return BlockResponse.builder()
                .id(block.getId())
                .propertyId(block.getPropertyId())
                .name(block.getName())
                .description(block.getDescription())
                .numberOfFloors(block.getNumberOfFloors())
                .build();
    }

    public static Block toEntity(BlockRequest request, UUID propertyId) {
        if (request == null) {
            return null;
        }

        return Block.builder()
                .propertyId(propertyId)
                .name(request.getName())
                .description(request.getDescription())
                .numberOfFloors(request.getNumberOfFloors() != null ? request.getNumberOfFloors() : 1)
                .build();
    }
}
