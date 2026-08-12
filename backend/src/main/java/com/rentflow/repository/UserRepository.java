package com.rentflow.repository;

import com.rentflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRolesName(String roleName);
    Page<User> findByOrganizationId(UUID organizationId, Pageable pageable);
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
