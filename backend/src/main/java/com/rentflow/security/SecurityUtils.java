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

    /**
     * The tenant boundary for the current request.
     *
     * @throws ResourceNotFoundException when the user exists but is not attached to an
     *                                   organization, which makes every scoped query meaningless
     */
    public static UUID getCurrentUserOrganizationId() {
        UUID organizationId = getCurrentUser().getUser().getOrganizationId();

        if (organizationId == null) {
            throw new ResourceNotFoundException("User does not belong to any organization");
        }

        return organizationId;
    }
}
