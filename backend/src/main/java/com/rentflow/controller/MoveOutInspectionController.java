package com.rentflow.controller;

import com.rentflow.dto.request.MoveOutInspectionRequest;
import com.rentflow.dto.response.MoveOutInspectionResponse;
import com.rentflow.service.MoveOutInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/move-out-inspections")
@RequiredArgsConstructor
public class MoveOutInspectionController {

    private final MoveOutInspectionService service;

    @PostMapping
    public ResponseEntity<MoveOutInspectionResponse> create(@RequestBody MoveOutInspectionRequest request) {
        MoveOutInspectionResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/move-out-inspections/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MoveOutInspectionResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoveOutInspectionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
