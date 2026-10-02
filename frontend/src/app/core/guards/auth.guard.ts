import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../auth/roles';

/**
 * The landing destination for a user, which differs by role: a tenant belongs in
 * the portal, everyone else in the dashboard. Shared by the guards below and by
 * `AuthService` after a successful login.
 */
export function homeUrlTree(authService: AuthService, router: Router): UrlTree {
  return router.createUrlTree([authService.hasRole(Role.Tenant) ? '/tenant' : '/dashboard']);
}

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Carry the attempted URL so a deep link survives the login round trip.
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};

/** Keeps a signed-in user off the login and register pages. */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  return homeUrlTree(authService, router);
};

/**
 * The marketing page is for signed-out visitors, but someone with a live session
 * who lands on `/` almost certainly wants their workspace rather than a sales
 * pitch, so they are forwarded instead.
 */
export const landingRedirectGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  return homeUrlTree(authService, router);
};
