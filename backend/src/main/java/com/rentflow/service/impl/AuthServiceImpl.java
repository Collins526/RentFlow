package com.rentflow.service.impl;

import com.rentflow.dto.request.LoginRequest;
import com.rentflow.dto.request.RefreshTokenRequest;
import com.rentflow.dto.request.RegisterRequest;
import com.rentflow.dto.response.AuthResponse;
import com.rentflow.dto.response.AuthUserDTO;
import com.rentflow.entity.RefreshToken;
import com.rentflow.entity.Role;
import com.rentflow.entity.User;
import com.rentflow.exception.BadRequestException;
import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import com.rentflow.repository.OrganizationRepository;
import com.rentflow.repository.RefreshTokenRepository;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.UserRepository;
import com.rentflow.security.JwtUtils;
import com.rentflow.security.UserDetailsImpl;
import com.rentflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user.getId());

        return buildAuthResponse(jwt, refreshToken.getToken(), user);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Create new Organization
        com.rentflow.entity.Organization organization = com.rentflow.entity.Organization.builder()
                .name(request.getOrganizationName())
                .build();
        organization = organizationRepository.save(organization);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status("ACTIVE")
                .organizationId(organization.getId())
                .build();

        // Assign Role
        Role orgOwnerRole = roleRepository.findByName("ORGANIZATION_OWNER")
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found. Please run DB migrations."));
        user.getRoles().add(orgOwnerRole);

        userRepository.save(user);

        // Authenticate the user directly after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user.getId());

        return buildAuthResponse(jwt, refreshToken.getToken(), user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(token -> {
                    // Refresh token rotation: Revoke old and generate new
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);

                    User user = token.getUser();
                    String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
                    RefreshToken newRefreshToken = createRefreshToken(user.getId());

                    return buildAuthResponse(jwt, newRefreshToken.getToken(), user);
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not in database or is revoked!"));
    }

    @Override
    @Transactional
    public void logout(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(refreshTokenRepository::deleteByUser);
    }

    @Override
    public AuthUserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return mapToAuthUserDTO(userDetails.getUser());
    }

    private RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Ensure only one active refresh token per user (or allow multiple for multi-device)
        // Here we keep it simple: clear old tokens
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token was revoked. Please make a new signin request");
        }
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToAuthUserDTO(user))
                .build();
    }

    private AuthUserDTO mapToAuthUserDTO(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toList());

        return AuthUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())
                .tenantId(user.getTenantId())
                .unitId(user.getUnitId())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
