import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';
import { of } from 'rxjs';
import { LayoutComponent } from './layout.component';
import { AuthService } from '../core/services/auth.service';
import { Role, RoleName } from '../core/auth/roles';

/** Renders the shell at a given viewport class for an organization owner. */
function renderLayout(handset: boolean) {
  TestBed.configureTestingModule({
    imports: [LayoutComponent],
    providers: [
      provideRouter([]),
      provideNoopAnimations(),
      {
        provide: BreakpointObserver,
        useValue: {
          observe: () => of({ matches: handset, breakpoints: {} } as BreakpointState)
        }
      },
      {
        provide: AuthService,
        useValue: {
          hasAnyRole: (allowed: readonly RoleName[]) =>
            allowed.length === 0 || allowed.includes(Role.OrganizationOwner),
          initials: () => 'CK',
          displayName: () => 'Collins Kibet',
          primaryRoleLabel: () => 'Organization Owner',
          currentUser: () => ({ email: 'owner@testorg.com' }),
          logout: () => {}
        }
      }
    ]
  });

  const fixture = TestBed.createComponent(LayoutComponent);
  fixture.detectChanges();
  return fixture;
}

/**
 * The test environment does not provide Web Storage, so the sidebar preference
 * gets an in-memory stand-in. The component treats storage as best-effort, which
 * is why it survives its absence at runtime too.
 */
function installStorageStub(): Storage {
  const entries = new Map<string, string>();
  const stub = {
    getItem: (key: string) => entries.get(key) ?? null,
    setItem: (key: string, value: string) => void entries.set(key, value),
    removeItem: (key: string) => void entries.delete(key),
    clear: () => entries.clear(),
    key: (index: number) => Array.from(entries.keys())[index] ?? null,
    get length() {
      return entries.size;
    }
  } as Storage;

  Object.defineProperty(globalThis, 'localStorage', { value: stub, configurable: true });
  return stub;
}

describe('LayoutComponent shell', () => {
  beforeEach(() => installStorageStub());

  afterEach(() => {
    TestBed.resetTestingModule();
    localStorage.clear();
  });

  it('composes sidebar, topbar and a routed outlet', () => {
    const el = renderLayout(false).nativeElement as HTMLElement;

    expect(el.querySelector('app-sidebar')).toBeTruthy();
    expect(el.querySelector('app-topbar')).toBeTruthy();
    expect(el.querySelector('router-outlet')).toBeTruthy();
  });

  it('docks the sidebar on desktop rather than overlaying it', () => {
    const el = renderLayout(false).nativeElement as HTMLElement;

    expect(el.querySelector('aside')).toBeTruthy();
    // No scrim means no overlay drawer.
    expect(el.querySelector('.fixed.inset-0')).toBeNull();
  });

  it('hides the sidebar behind a drawer on handsets until it is opened', () => {
    const fixture = renderLayout(true);
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('app-sidebar')).toBeNull();

    (el.querySelector('button[aria-label="Toggle navigation"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('app-sidebar')).toBeTruthy();
    expect(el.querySelector('.fixed.inset-0')).toBeTruthy();
  });

  it('remembers the collapsed rail across sessions on desktop', () => {
    const fixture = renderLayout(false);
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('button[aria-label="Toggle navigation"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(localStorage.getItem('rf.sidebar.collapsed')).toBe('true');
  });
});
