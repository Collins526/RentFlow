package com.rentflow.config;

import com.rentflow.entity.Role;
import com.rentflow.entity.User;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminProperties superAdminProperties;

    @Override
    @Transactional
    public void run(String... args) {
        Role platformAdminRole = findOrCreateRole("PLATFORM_ADMIN", "Platform Administrator");
        findOrCreateRole("ORGANIZATION_ADMIN", "Organization Administrator");
        findOrCreateRole("ORGANIZATION_OWNER", "Organization Owner");
        findOrCreateRole("PROPERTY_MANAGER", "Property Manager");
        findOrCreateRole("ACCOUNTANT", "Accountant");
        findOrCreateRole("TENANT", "Tenant");

        if (!userRepository.existsByRolesName("PLATFORM_ADMIN")) {
            String email = superAdminProperties.getEmail();
            Optional<User> existingSuperAdmin = userRepository.findByEmail(email);
            if (existingSuperAdmin.isPresent()) {
                User user = existingSuperAdmin.get();
                if (user.getRoles().stream().noneMatch(role -> "PLATFORM_ADMIN".equals(role.getName()))) {
                    user.getRoles().add(platformAdminRole);
                    userRepository.save(user);
                    log.info("Assigned PLATFORM_ADMIN role to existing user '{}'.", email);
                }
            } else {
                User superAdmin = User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(superAdminProperties.getPassword()))
                        .firstName(superAdminProperties.getFirstName())
                        .lastName(superAdminProperties.getLastName())
                        .status("ACTIVE")
                        .build();
                superAdmin.getRoles().add(platformAdminRole);
                userRepository.save(superAdmin);
                log.info("Created default super admin user '{}' with PLATFORM_ADMIN role.", email);
            }
        }
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }
}
