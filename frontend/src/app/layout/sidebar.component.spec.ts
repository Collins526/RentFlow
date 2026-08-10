import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { SidebarComponent } from './sidebar.component';
import { AuthService } from '../core/services/auth.service';
import { Role, RoleName } from '../core/auth/roles';

/** Renders the sidebar as it would appear for a user holding exactly `roles`. */
function renderFor(roles: RoleName[]) {
  const held = new Set<string>(roles);

  TestBed.configureTestingModule({
    imports: [SidebarComponent],
    providers: [
      provideRouter([]),
      provideNoopAnimations(),
      {
        provide: AuthService,
        useValue: {
          hasAnyRole: (allowed: readonly RoleName[]) =>
            allowed.length === 0 || allowed.some(role => held.has(role))
        }
      }
    ]
  });

  const fixture = TestBed.createComponent(SidebarComponent);
  fixture.detectChanges();
  return fixture;
}

/**
 * Reads the label span rather than the anchor's textContent, which would also
 * pick up the mat-icon ligature.
 */
function labelsOf(fixture: ReturnType<typeof renderFor>): string[] {
  return Array.from(fixture.nativeElement.querySelectorAll('a span'))
    .map(span => (span as HTMLElement).textContent?.trim() ?? '')
    .filter(Boolean);
}

describe('SidebarComponent role-based navigation', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('shows the full portfolio to an organization owner', () => {
    const labels = labelsOf(renderFor([Role.OrganizationOwner]));

    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Properties');
    expect(labels).toContain('Tenants');
    expect(labels).toContain('Tenancies');
    expect(labels).toContain('Organization');
  });

  it('hides Organization from a property manager', () => {
    const labels = labelsOf(renderFor([Role.PropertyManager]));

    expect(labels).toContain('Properties');
    expect(labels).toContain('Tenancies');
    expect(labels).not.toContain('Organization');
  });

  it('gives an accountant read areas but no administration', () => {
    const labels = labelsOf(renderFor([Role.Accountant]));

    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Tenants');
    expect(labels).not.toContain('Organization');
  });

  it('shows nothing to a tenant, who has no back-office access', () => {
    expect(labelsOf(renderFor([Role.Tenant]))).toEqual([]);
  });

  it('drops a section heading once all of its items are filtered out', () => {
    const fixture = renderFor([Role.Accountant]);
    const headings = Array.from(fixture.nativeElement.querySelectorAll('p'))
      .map(node => (node as HTMLElement).textContent?.trim());

    expect(headings).not.toContain('Administration');
  });
});
