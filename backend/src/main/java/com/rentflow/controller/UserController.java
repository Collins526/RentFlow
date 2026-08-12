package com.rentflow.controller;

import com.rentflow.common.ApiResponse;
import com.rentflow.dto.request.CreateUserRequest;
import com.rentflow.dto.response.UserResponse;
import com.rentflow.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final String ADMIN_ROLES =
            "hasRole('ORGANIZATION_OWNER') or hasRole('ORGANIZATION_ADMIN') or hasRole('PLATFORM_ADMIN')";
    private static final String USER_READ_ROLES = ADMIN_ROLES;

    private final UserService userService;

    @PostMapping
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return new ResponseEntity<>(ApiResponse.success(user, "User created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(USER_READ_ROLES)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(USER_READ_ROLES)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User fetched successfully"));
    }
}
