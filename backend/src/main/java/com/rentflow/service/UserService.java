package com.rentflow.service;

import com.rentflow.dto.request.CreateUserRequest;
import com.rentflow.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse getUserById(UUID id);
}
