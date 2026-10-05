import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { AuthService } from './auth.service';

describe('AuthService inactivity timeout', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    const storage = new Map<string, string>();
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => storage.set(key, value),
      removeItem: (key: string) => storage.delete(key)
    });
    localStorage.setItem('accessToken', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 'user-1',
      email: 'user@example.com',
      roles: [],
      permissions: []
    }));
    localStorage.removeItem('lastActivityAt');

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: {
            routerState: { snapshot: { root: { queryParams: {} } } },
            navigate: vi.fn(),
            navigateByUrl: vi.fn()
          }
        }
      ]
    });
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    vi.useRealTimers();
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('lastActivityAt');
    vi.unstubAllGlobals();
  });

  it('logs out after five minutes without activity', () => {
    vi.useFakeTimers();
    const auth = TestBed.inject(AuthService);

    vi.advanceTimersByTime(5 * 60 * 1000);

    expect(auth.isAuthenticated()).toBe(false);
    httpTesting.expectOne('/api/v1/auth/logout').flush({});
  });

  it('restarts the timeout when the user is active', () => {
    vi.useFakeTimers();
    const auth = TestBed.inject(AuthService);

    vi.advanceTimersByTime(4 * 60 * 1000);
    window.dispatchEvent(new Event('keydown'));
    vi.advanceTimersByTime(4 * 60 * 1000);
    expect(auth.isAuthenticated()).toBe(true);

    vi.advanceTimersByTime(60 * 1000);
    expect(auth.isAuthenticated()).toBe(false);
    httpTesting.expectOne('/api/v1/auth/logout').flush({});
  });
});