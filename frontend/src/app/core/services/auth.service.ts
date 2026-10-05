import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { Role, RoleName } from '../auth/roles';

const INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000;
const LAST_ACTIVITY_KEY = 'lastActivityAt';
const ACTIVITY_EVENTS = ['pointerdown', 'keydown', 'mousemove', 'touchstart', 'scroll'] as const;

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
  private inactivityTimer: ReturnType<typeof setTimeout> | null = null;
  
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
    this.installActivityListeners();
    if (this.isAuthenticated()) {
      this.scheduleInactivityLogout();
    }
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
    removeStored(LAST_ACTIVITY_KEY);
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
      error: () => {}
    });
    this.clearSession();
  }

  private handleAuthSuccess(data: AuthResponse) {
    writeStored('accessToken', data.accessToken);
    writeStored('refreshToken', data.refreshToken);
    writeStored('user', JSON.stringify(data.user));

    this.currentUser.set(data.user);
    this.isAuthenticated.set(true);
    this.recordActivity();

    // Honour the deep link the guard stashed, so a bookmarked page survives login.
    const returnUrl = this.router.routerState.snapshot.root.queryParams['returnUrl'];
    const safeReturnUrl = returnUrl && returnUrl !== '/forbidden' ? returnUrl : null;
    const redirectTo = safeReturnUrl ?? (this.hasRole(Role.Tenant) ? '/tenant' : '/dashboard');
    this.router.navigateByUrl(redirectTo);
  }

  private clearSession() {
    this.stopInactivityTimer();
    this.clearStoredSession();

    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return readStored('accessToken');
  }

  private installActivityListeners(): void {
    if (typeof window === 'undefined') {
      return;
    }

    for (const eventName of ACTIVITY_EVENTS) {
      window.addEventListener(eventName, this.recordActivity);
    }
    window.addEventListener('storage', this.handleStorageActivity);
    document.addEventListener('visibilitychange', this.scheduleInactivityLogout);
  }

  private recordActivity = (event?: Event): void => {
    if (!this.isAuthenticated()) {
      return;
    }

    const now = Date.now();
    const lastActivityAt = Number(readStored(LAST_ACTIVITY_KEY));
    if (event?.type === 'mousemove' && Number.isFinite(lastActivityAt) && now - lastActivityAt < 1000) {
      return;
    }

    writeStored(LAST_ACTIVITY_KEY, String(now));
    this.scheduleInactivityLogout();
  };

  private handleStorageActivity = (event: StorageEvent): void => {
    if (event.key === LAST_ACTIVITY_KEY && this.isAuthenticated()) {
      this.scheduleInactivityLogout();
    }
  };

  private scheduleInactivityLogout = (): void => {
    if (!this.isAuthenticated()) {
      return;
    }

    this.stopInactivityTimer();
    const storedActivity = readStored(LAST_ACTIVITY_KEY);
    const lastActivityAt = Number(storedActivity);
    if (!storedActivity || !Number.isFinite(lastActivityAt) || lastActivityAt <= 0) {
      writeStored(LAST_ACTIVITY_KEY, String(Date.now()));
    }
    const elapsed = storedActivity && Number.isFinite(lastActivityAt) && lastActivityAt > 0
      ? Math.max(0, Date.now() - lastActivityAt)
      : 0;
    const remaining = INACTIVITY_TIMEOUT_MS - elapsed;

    if (remaining <= 0) {
      this.logout();
      return;
    }

    this.inactivityTimer = setTimeout(this.scheduleInactivityLogout, remaining);
  };

  private stopInactivityTimer(): void {
    if (this.inactivityTimer !== null) {
      clearTimeout(this.inactivityTimer);
      this.inactivityTimer = null;
    }
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
