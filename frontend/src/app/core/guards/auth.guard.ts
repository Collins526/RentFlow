import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../auth/roles';

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

  return router.createUrlTree([authService.hasRole(Role.Tenant) ? '/tenant' : '/dashboard']);
};
