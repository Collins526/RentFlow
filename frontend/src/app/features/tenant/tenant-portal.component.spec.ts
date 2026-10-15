import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification/notification.service';
import { TenantPortalService } from '../../core/services/tenant/tenant-portal.service';
import { ToastService } from '../../core/services/toast.service';
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
            getUnreadInvoiceNotifications: () => of({
              success: true,
              message: '',
              errors: [],
              data: { content: [] }
            }),
            getPayments: () => of({ success: true, message: '', errors: [], data: { content: [] } }),
            getMaintenanceRequests: () => of({ success: true, message: '', errors: [], data: { content: [
              { id: 'request-1', tenantId: 'tenant-1', title: 'Leaking kitchen tap', status: 'IN_PROGRESS' }
            ] } }),
            getMyActiveTenancy: () => of({ success: true, message: '', errors: [], data: {
              id: 'tenancy-1', unitId: 'unit-1', unitNumber: 'B-12', rentAmount: 35000,
              securityDepositAmount: 35000,
              startDate: '2026-04-01', endDate: '2027-03-31', status: 'ACTIVE'
            } })
          }
        },
        {
          provide: NotificationService,
          useValue: {
            getMyAnnouncements: () => of({
              success: true,
              message: '',
              errors: [],
              data: { content: [{
                id: 'announcement-1',
                title: 'Water maintenance',
                message: 'Water will be unavailable on Friday.',
                createdAt: '2026-10-01T10:00:00'
              }] }
            }),
            markMyAnnouncementsRead: () => of({
              success: true,
              message: '',
              errors: [],
              data: undefined
            })
          }
        },
        {
          provide: ToastService,
          useValue: { apiError: () => {}, error: () => {} }
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
    expect(page.textContent).toContain('Water maintenance');
    expect(page.textContent).toContain('Water will be unavailable on Friday.');

    const rentLink = Array.from(page.querySelectorAll('a'))
      .find(link => link.textContent?.trim() === 'Pay rent');
    expect(rentLink?.getAttribute('href')).toContain('paymentType=rent');
    expect(rentLink?.getAttribute('href')).toContain('invoiceId=invoice-12345678');

    const depositLink = Array.from(page.querySelectorAll('a'))
      .find(link => link.textContent?.trim() === 'Pay security deposit');
    expect(depositLink?.getAttribute('href')).toContain('paymentType=security-deposit');
    expect(depositLink?.getAttribute('href')).toContain('amount=35000');
  });

  it('opens the rent payment form when there is no outstanding invoice', () => {
    const service = TestBed.inject(TenantPortalService);
    vi.spyOn(service, 'getInvoices').mockReturnValue(of({
      success: true,
      message: '',
      errors: [],
      data: { content: [], totalElements: 0, totalPages: 0, size: 100, number: 0 }
    }));

    const fixture = TestBed.createComponent(TenantPortalComponent);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;
    const rentLink = Array.from(page.querySelectorAll('a'))
      .find(link => link.textContent?.trim() === 'Pay rent');

    expect(rentLink?.getAttribute('href')).toContain('paymentType=rent');
  });

  it('shows unread invoice notifications and links directly to the invoice', () => {
    const service = TestBed.inject(TenantPortalService);
    vi.spyOn(service, 'getUnreadInvoiceNotifications').mockReturnValue(of({
      success: true,
      message: '',
      errors: [],
      data: {
        content: [{
          id: 'notification-1',
          invoiceId: 'invoice-12345678',
          title: 'New rent invoice',
          message: 'A rent invoice for KES 35000 is ready to view.'
        }],
        totalElements: 1,
        totalPages: 1,
        size: 100,
        number: 0
      }
    }));

    const fixture = TestBed.createComponent(TenantPortalComponent);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;
    const notificationLink = Array.from(page.querySelectorAll('a'))
      .find(link => link.textContent?.includes('New rent invoice'));

    expect(notificationLink?.textContent).toContain('ready to view');
    expect(notificationLink?.getAttribute('href')).toContain('invoiceId=invoice-12345678');
  });
});