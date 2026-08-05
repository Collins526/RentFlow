package com.rentflow.service.impl;

import com.rentflow.dto.request.OrganizationUpdateRequest;
import com.rentflow.dto.response.OrganizationResponse;
import com.rentflow.entity.Organization;
import com.rentflow.entity.User;
import com.rentflow.repository.OrganizationRepository;
import com.rentflow.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private User mockUser;
    private Organization mockOrganization;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        mockOrganization = Organization.builder()
                .name("Test Org")
                .email("info@testorg.com")
                .build();
        mockOrganization.setId(orgId);

        mockUser = User.builder()
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
    void getMyOrganization_Success() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mockOrganization));

        OrganizationResponse response = organizationService.getMyOrganization();

        assertNotNull(response);
        assertEquals("Test Org", response.getName());
        assertEquals("info@testorg.com", response.getEmail());
        verify(organizationRepository).findById(orgId);
    }

    @Test
    void updateMyOrganization_Success() {
        OrganizationUpdateRequest request = new OrganizationUpdateRequest();
        request.setName("Updated Org Name");
        request.setEmail("updated@testorg.com");
        request.setPhone("1234567890");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mockOrganization));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArguments()[0]);

        OrganizationResponse response = organizationService.updateMyOrganization(request);

        assertNotNull(response);
        assertEquals("Updated Org Name", response.getName());
        assertEquals("updated@testorg.com", response.getEmail());
        assertEquals("1234567890", response.getPhone());
        
        verify(organizationRepository).save(any(Organization.class));
    }
}
