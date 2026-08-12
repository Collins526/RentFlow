package com.rentflow.service.impl;

import com.rentflow.dto.request.CreateUserRequest;
import com.rentflow.dto.response.UserResponse;
import com.rentflow.entity.Role;
import com.rentflow.entity.User;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.UserRepository;
import com.rentflow.security.SecurityUtils;
import com.rentflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private boolean isPlatformAdmin() {
        return SecurityUtils.isPlatformAdmin();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UUID orgId;
        if (isPlatformAdmin()) {
            if (request.getOrganizationId() == null) {
                throw new BadRequestException("organizationId is required when creating a user as PLATFORM_ADMIN");
            }
            orgId = request.getOrganizationId();
        } else {
            orgId = SecurityUtils.getCurrentUserOrganizationId();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .organizationId(orgId)
                .tenantId(request.getTenantId())
                .unitId(request.getUnitId())
                .status("ACTIVE")
                .build();

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role tenantRole = roleRepository.findByName("TENANT")
                    .orElseThrow(() -> new ResourceNotFoundException("Role TENANT not found. Please run DB migrations."));
            user.getRoles().add(tenantRole);
        } else {
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found. Please run DB migrations."));
                user.getRoles().add(role);
            });
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        if (isPlatformAdmin()) {
            return userRepository.findAll(pageable)
                    .map(this::toResponse);
        }

        UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
        return userRepository.findByOrganizationId(orgId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user;
        if (isPlatformAdmin()) {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        } else {
            UUID orgId = SecurityUtils.getCurrentUserOrganizationId();
            user = userRepository.findByIdAndOrganizationId(id, orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        }
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())
                .tenantId(user.getTenantId())
                .unitId(user.getUnitId())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }
}
