import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { Role, RoleName } from '../auth/roles';

export interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId?: string | null;
  tenantId?: string | null;
  unitId?: string | null;
  roles: string[];
  permissions: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: AuthUser;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/v1/auth';
  
  // Using Signals for state management
  currentUser = signal<AuthUser | null>(null);
  isAuthenticated = signal<boolean>(false);

  /** Roles held by the signed-in user, as a set for cheap repeated lookups in the nav. */
  private roleSet = computed(() => new Set(this.currentUser()?.roles ?? []));

  displayName = computed(() => {
    const user = this.currentUser();
    if (!user) {
      return '';
    }
    return `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email;
  });

  /** Up to two letters for the profile avatar, falling back to the email. */
  initials = computed(() => {
    const user = this.currentUser();
    if (!user) {
      return '';
    }
    const letters = [user.firstName?.[0], user.lastName?.[0]].filter(Boolean).join('');
    return (letters || user.email?.[0] || '').toUpperCase();
  });

  /** Human-readable form of the user's primary role, e.g. "Property Manager". */
  primaryRoleLabel = computed(() => {
    const role = this.currentUser()?.roles?.[0];
    if (!role) {
      return '';
    }
    return role
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  });

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
  }

  hasRole(role: RoleName): boolean {
    return this.roleSet().has(role);
  }

  /** True when the user holds at least one of `roles`. An empty list means "any signed-in user". */
  hasAnyRole(roles: readonly RoleName[]): boolean {
    if (roles.length === 0) {
      return true;
    }
    const held = this.roleSet();
    return roles.some(role => held.has(role));
  }

  private loadUserFromStorage() {
    const userJson = readStored('user');
    const token = readStored('accessToken');
    if (!userJson || !token) {
      return;
    }

    // This runs in a root service constructor, so a corrupt entry would otherwise
    // take down app bootstrap. Drop the bad session and let the guard redirect.
    try {
      this.currentUser.set(JSON.parse(userJson));
      this.isAuthenticated.set(true);
    } catch {
      this.clearStoredSession();
    }
  }

  private clearStoredSession() {
    removeStored('user');
    removeStored('accessToken');
    removeStored('refreshToken');
  }

  login(credentials: any): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.success) {
          this.handleAuthSuccess(response.data);
        }
      })
    );
  }

  register(userData: any): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, userData).pipe(
      tap(response => {
        if (response.success) {
          this.handleAuthSuccess(response.data);
        }
      })
    );
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private handleAuthSuccess(data: AuthResponse) {
    writeStored('accessToken', data.accessToken);
    writeStored('refreshToken', data.refreshToken);
    writeStored('user', JSON.stringify(data.user));

    this.currentUser.set(data.user);
    this.isAuthenticated.set(true);

    // Honour the deep link the guard stashed, so a bookmarked page survives login.
    const returnUrl = this.router.routerState.snapshot.root.queryParams['returnUrl'];
    const safeReturnUrl = returnUrl && returnUrl !== '/forbidden' ? returnUrl : null;
    const redirectTo = safeReturnUrl ?? (this.hasRole(Role.Tenant) ? '/tenant' : '/dashboard');
    this.router.navigateByUrl(redirectTo);
  }

  private clearSession() {
    this.clearStoredSession();

    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return readStored('accessToken');
  }
}

/**
 * Web Storage is not guaranteed: it is absent under SSR and throws outright in
 * some privacy modes. Session persistence is a convenience, so every access is
 * best-effort and the in-memory signals remain the source of truth for a request.
 */
function readStored(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeStored(key: string, value: string): void {
  try {
    localStorage.setItem(key, value);
  } catch {
    // Session survives in memory for this page load only.
  }
}

function removeStored(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch {
    // Nothing to do if storage is unavailable.
  }
}
