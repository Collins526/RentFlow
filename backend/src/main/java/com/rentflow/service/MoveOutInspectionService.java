package com.rentflow.service;

import com.rentflow.dto.request.MoveOutInspectionRequest;
import com.rentflow.dto.response.MoveOutInspectionResponse;

import java.util.List;
import java.util.UUID;

public interface MoveOutInspectionService {
    MoveOutInspectionResponse create(MoveOutInspectionRequest request);
    List<MoveOutInspectionResponse> findAll();
    MoveOutInspectionResponse findById(UUID id);
    void delete(UUID id);
}
