import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { Role } from '../auth/roles';

describe('roleGuard', () => {
  let auth: { isAuthenticated: ReturnType<typeof vi.fn>; hasAnyRole: ReturnType<typeof vi.fn> };

  const run = (allowed: string[], url = '/tenancies') => {
    const guard = roleGuard(allowed as any);
    const state = { url } as RouterStateSnapshot;
    return TestBed.runInInjectionContext(() => guard({} as ActivatedRouteSnapshot, state));
  };

  beforeEach(() => {
    auth = { isAuthenticated: vi.fn(), hasAnyRole: vi.fn() };

    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }]
    });
  });

  it('allows a user holding one of the required roles', () => {
    auth.isAuthenticated.mockReturnValue(true);
    auth.hasAnyRole.mockReturnValue(true);

    expect(run([Role.OrganizationOwner])).toBe(true);
  });

  it('sends a signed-out user to login carrying the attempted url', () => {
    auth.isAuthenticated.mockReturnValue(false);

    const result = run([Role.OrganizationOwner], '/tenancies?page=2');
    const router = TestBed.inject(Router);

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree))
      .toBe('/login?returnUrl=%2Ftenancies%3Fpage%3D2');
  });

  it('sends a signed-in user without the role to forbidden, not back to login', () => {
    auth.isAuthenticated.mockReturnValue(true);
    auth.hasAnyRole.mockReturnValue(false);

    const result = run([Role.OrganizationOwner]);
    const router = TestBed.inject(Router);

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/forbidden');
  });
});
