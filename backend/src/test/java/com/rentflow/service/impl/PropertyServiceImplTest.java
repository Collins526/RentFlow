package com.rentflow.service.impl;

import com.rentflow.dto.request.PropertyRequest;
import com.rentflow.dto.response.PropertyResponse;
import com.rentflow.entity.Property;
import com.rentflow.entity.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private User mockUser;
    private Property mockProperty;
    private UUID orgId;
    private UUID propertyId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        mockProperty = Property.builder()
                .organizationId(orgId)
                .name("Sunnyvale Apartments")
                .type("RESIDENTIAL")
                .address("123 Sunny St")
                .status("ACTIVE")
                .build();
        mockProperty.setId(propertyId);

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
    void createProperty_Success() {
        PropertyRequest request = new PropertyRequest();
        request.setName("New Property");
        request.setType("COMMERCIAL");
        request.setAddress("456 New St");
        
        when(propertyRepository.save(any(Property.class))).thenAnswer(i -> {
            Property p = (Property) i.getArguments()[0];
            p.setId(UUID.randomUUID());
            return p;
        });

        PropertyResponse response = propertyService.createProperty(request);

        assertNotNull(response);
        assertEquals("New Property", response.getName());
        assertEquals("COMMERCIAL", response.getType());
        assertEquals(orgId, response.getOrganizationId());
        
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void getAllProperties_Success() {
        Page<Property> propertyPage = new PageImpl<>(Collections.singletonList(mockProperty));
        when(propertyRepository.findByOrganizationId(eq(orgId), any(PageRequest.class))).thenReturn(propertyPage);

        Page<PropertyResponse> result = propertyService.getAllProperties(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Sunnyvale Apartments", result.getContent().get(0).getName());
    }

    @Test
    void updateProperty_Success() {
        PropertyRequest request = new PropertyRequest();
        request.setName("Updated Apartments");
        request.setType("RESIDENTIAL");
        request.setAddress("123 Sunny St");

        when(propertyRepository.findByIdAndOrganizationId(propertyId, orgId)).thenReturn(Optional.of(mockProperty));
        when(propertyRepository.save(any(Property.class))).thenAnswer(i -> i.getArguments()[0]);

        PropertyResponse response = propertyService.updateProperty(propertyId, request);

        assertNotNull(response);
        assertEquals("Updated Apartments", response.getName());
        verify(propertyRepository).save(any(Property.class));
    }
}
