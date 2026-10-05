import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { TenantPortalService } from '../../core/services/tenant/tenant-portal.service';
import { TenantPortalComponent } from './tenant-portal.component';

describe('TenantPortalComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TenantPortalComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            currentUser: () => ({ firstName: 'Kiptoo', tenantId: 'tenant-1' })
          }
        },
        {
          provide: TenantPortalService,
          useValue: {
            getInvoices: () => of({ success: true, message: '', errors: [], data: { content: [
              {
                id: 'invoice-12345678',
                tenantId: 'tenant-1',
                unitId: 'unit-1',
                unitNumber: 'B-12',
                amount: 35000,
                status: 'ISSUED',
                periodStart: '2026-10-01',
                dueDate: '2026-10-10'
              }
            ] } }),
            getPayments: () => of({ success: true, message: '', errors: [], data: { content: [] } }),
            getMaintenanceRequests: () => of({ success: true, message: '', errors: [], data: { content: [
              { id: 'request-1', tenantId: 'tenant-1', title: 'Leaking kitchen tap', status: 'IN_PROGRESS' }
            ] } }),
            getMyActiveTenancy: () => of({ success: true, message: '', errors: [], data: {
              id: 'tenancy-1', unitId: 'unit-1', unitNumber: 'B-12', rentAmount: 35000,
              startDate: '2026-04-01', endDate: '2027-03-31', status: 'ACTIVE'
            } })
          }
        }
      ]
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('renders the tenant rent, unit, and open request summaries', () => {
    const fixture = TestBed.createComponent(TenantPortalComponent);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;

    expect(page.textContent).toContain('Welcome back, Kiptoo');
    expect(page.textContent).toContain('KES 35,000');
    expect(page.textContent).toContain('B-12');
    expect(page.textContent).toContain('1 open');
    expect(page.textContent).toContain('Leaking kitchen tap');
  });
});