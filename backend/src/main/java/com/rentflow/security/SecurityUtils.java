package com.rentflow.security;

import com.rentflow.exception.ResourceNotFoundException;
import com.rentflow.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Static access to the authenticated principal.
 * <p>
 * Multi-tenancy in RentFlow is enforced per request by scoping every query to the
 * caller's organization, so services need the current organization id constantly.
 * Resolving it here keeps that lookup — and its failure modes — in one place.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * @throws UnauthorizedException when the request carries no authenticated principal
     */
    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return userDetails;
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }

    public static boolean isPlatformAdmin() {
        UserDetailsImpl currentUser = getCurrentUser();
        return currentUser.getUser().getRoles().stream()
            .anyMatch(role -> "PLATFORM_ADMIN".equals(role.getName()))
            || currentUser.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_PLATFORM_ADMIN".equals(authority.getAuthority()));
    }

    public static boolean hasRole(String roleName) {
        String authority = "ROLE_" + roleName;
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }

    public static UUID getCurrentUserOrganizationIdOrNull() {
        return getCurrentUser().getUser().getOrganizationId();
    }

    public static UUID getCurrentUserTenantIdOrNull() {
        return getCurrentUser().getUser().getTenantId();
    }

    /**
     * The tenant boundary for the current request.
     *
     * @throws ResourceNotFoundException when the user exists but is not attached to an
     *                                   organization, which makes every scoped query meaningless
     */
    public static UUID getCurrentUserOrganizationId() {
        UUID organizationId = getCurrentUserOrganizationIdOrNull();

        if (organizationId == null) {
            throw new ResourceNotFoundException("User does not belong to any organization");
        }

        return organizationId;
    }
}
