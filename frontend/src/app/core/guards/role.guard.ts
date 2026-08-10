import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RoleName } from '../auth/roles';

/**
 * Route gate for a set of roles. This complements — it does not replace — the
 * backend's `@PreAuthorize` rules: it keeps a user from landing on a page whose
 * every request would 403, but the server remains the authority.
 *
 * Signed-out users go to the login page carrying a returnUrl; signed-in users
 * without the role get the "forbidden" page rather than a login loop.
 */
export function roleGuard(allowed: readonly RoleName[]): CanActivateFn {
  return (_route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url }
      });
    }

    if (authService.hasAnyRole(allowed)) {
      return true;
    }

    return router.createUrlTree(['/forbidden']);
  };
}
