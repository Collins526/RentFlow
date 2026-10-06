import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import {
  TenantInvoice,
  TenantMaintenanceRequest,
  TenantPayment,
  TenantPortalService,
  TenantTenancy
} from '../../core/services/tenant/tenant-portal.service';

interface TenantActivity {
  id: string;
  icon: string;
  title: string;
  description: string;
  date: string;
  tone: string;
}

@Component({
  selector: 'app-tenant-portal',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  template: `
    <div class="mx-auto max-w-5xl space-y-3 text-sm text-slate-100">
      <div *ngIf="loading()" class="flex min-h-72 items-center justify-center text-slate-400" role="status">
        Loading your dashboard…
      </div>

      <div *ngIf="error()" class="rounded-lg border border-rose-300/20 bg-rose-950/40 p-4 text-rose-200" role="alert">
        {{ error() }}
      </div>

      <ng-container *ngIf="!loading() && !error()">
        <div class="mb-4 flex items-end justify-between gap-4">
          <div>
            <p class="text-[11px] font-semibold uppercase tracking-wider text-indigo-300">Tenant portal</p>
            <h1 class="mt-1 text-xl font-bold text-white">Welcome back, {{ firstName() }}</h1>
          </div>
          <p *ngIf="tenancy()" class="flex items-center gap-1.5 rounded-full border border-emerald-400/20 bg-emerald-400/10 px-2.5 py-1 text-[11px] font-medium text-emerald-300">
            <span class="h-1.5 w-1.5 rounded-full bg-emerald-400"></span>
            Active tenancy
          </p>
        </div>

        <section class="grid gap-3 md:grid-cols-[1.2fr_1fr]">
          <article class="rounded-xl border border-indigo-400/35 bg-[#171f48] p-4 shadow-sm">
            <p class="text-xs font-medium text-indigo-200">{{ dueInvoice() ? 'Current rent' : 'Rent overview' }}</p>
            <p class="mt-1 text-3xl font-bold tracking-tight text-white">KES {{ (monthlyRent() | number) }}</p>
            <p class="mt-1 text-xs text-indigo-200">
              <ng-container *ngIf="dueInvoice(); else noDueInvoice">
                {{ invoicePeriod(dueInvoice()!) }} · due {{ dueInvoice()!.dueDate | date:'MMM d' }} · {{ dueCountdown() }}
              </ng-container>
              <ng-template #noDueInvoice>{{ tenancy() ? 'No outstanding invoice' : 'No active tenancy is linked to your account' }}</ng-template>
            </p>
            <div class="mt-4 flex flex-wrap gap-2">
              <a routerLink="/tenant/payments" [queryParams]="paymentQuery()" class="rounded-md bg-indigo-300 px-3 py-2 text-xs font-semibold text-slate-950 hover:bg-indigo-200">
                Pay rent
              </a>
              <a *ngIf="tenancy()" routerLink="/tenant/payments" [queryParams]="securityDepositQuery()" class="rounded-md border border-white/15 px-3 py-2 text-xs font-semibold text-white hover:bg-white/5">
                Pay security deposit
              </a>
              <a *ngIf="dueInvoice()" routerLink="/tenant/invoices" class="rounded-md border border-white/15 px-3 py-2 text-xs font-semibold text-white hover:bg-white/5">
                View invoice
              </a>
            </div>
          </article>

          <article class="rounded-xl border border-slate-700 bg-[#111a31] p-4">
            <h2 class="text-xs font-semibold text-white">Your unit</h2>
            <dl class="mt-2 divide-y divide-slate-700/80 text-[11px]">
              <div class="flex justify-between gap-3 py-2">
                <dt class="text-slate-400">Unit</dt>
                <dd class="text-right font-semibold text-slate-100">{{ tenancy()?.unitNumber || (tenancy() ? tenancy()!.unitId.slice(0, 8) : 'Not linked') }}</dd>
              </div>
              <div class="flex justify-between gap-3 py-2">
                <dt class="text-slate-400">Monthly rent</dt>
                <dd class="font-semibold text-slate-100">KES {{ monthlyRent() | number }}</dd>
              </div>
              <div class="flex justify-between gap-3 py-2">
                <dt class="text-slate-400">Lease ends</dt>
                <dd class="font-semibold text-slate-100">{{ tenancy()?.endDate ? (tenancy()!.endDate | date:'MMM d, y') : 'Not specified' }}</dd>
              </div>
              <div class="flex justify-between gap-3 py-2">
                <dt class="text-slate-400">Lease progress</dt>
                <dd class="font-semibold text-slate-100">{{ leaseProgressLabel() }}</dd>
              </div>
            </dl>
            <div class="h-1 overflow-hidden rounded-full bg-slate-700">
              <div class="h-full rounded-full bg-indigo-400 transition-[width]" [style.width.%]="leaseProgress()"></div>
            </div>
          </article>
        </section>

        <section class="grid grid-cols-1 gap-3 sm:grid-cols-3" aria-label="Account summary">
          <a routerLink="/tenant/invoices" class="rounded-xl border border-slate-700 bg-[#111a31] p-3 transition hover:border-indigo-400/50">
            <span class="grid h-7 w-7 place-items-center rounded-lg bg-indigo-400/10 text-indigo-300"><mat-icon class="!h-4 !w-4 !text-base">receipt_long</mat-icon></span>
            <p class="mt-2 text-[11px] text-slate-400">Invoices</p>
            <p class="mt-0.5 font-semibold text-white">{{ unpaidInvoices().length }} unpaid</p>
            <p class="text-[10px] text-slate-500">{{ dueInvoice()?.dueDate ? ('Next due ' + (dueInvoice()!.dueDate | date:'MMM d')) : 'All caught up' }}</p>
          </a>
          <a routerLink="/tenant/payments" class="rounded-xl border border-slate-700 bg-[#111a31] p-3 transition hover:border-emerald-400/50">
            <span class="grid h-7 w-7 place-items-center rounded-lg bg-emerald-400/10 text-emerald-300"><mat-icon class="!h-4 !w-4 !text-base">payments</mat-icon></span>
            <p class="mt-2 text-[11px] text-slate-400">Last payment</p>
            <p class="mt-0.5 font-semibold text-white">{{ latestPayment() ? ('KES ' + (latestPayment()!.amount | number)) : 'No payments yet' }}</p>
            <p class="text-[10px] text-slate-500">{{ latestPayment()?.method || 'Payment history' }}<ng-container *ngIf="latestPayment()?.paymentDate"> · {{ latestPayment()!.paymentDate | date:'MMM d' }}</ng-container></p>
          </a>
          <a routerLink="/tenant/requests" class="rounded-xl border border-slate-700 bg-[#111a31] p-3 transition hover:border-amber-400/50">
            <span class="grid h-7 w-7 place-items-center rounded-lg bg-amber-400/10 text-amber-300"><mat-icon class="!h-4 !w-4 !text-base">build</mat-icon></span>
            <p class="mt-2 text-[11px] text-slate-400">Requests</p>
            <p class="mt-0.5 font-semibold text-white">{{ openRequests().length }} open</p>
            <p class="truncate text-[10px] text-slate-500">{{ openRequests()[0]?.title || 'No open requests' }}</p>
          </a>
        </section>

        <section class="grid gap-3 lg:grid-cols-[1.2fr_1fr]">
          <article class="min-h-64 rounded-xl border border-slate-700 bg-[#111a31] p-4">
            <div class="flex items-center justify-between">
              <h2 class="text-xs font-semibold text-white">Recent activity</h2>
              <a routerLink="/tenant/invoices" class="text-[10px] font-medium text-indigo-300 hover:text-white">See all</a>
            </div>
            <div *ngIf="recentActivity().length; else emptyActivity" class="mt-3 space-y-3">
              <div *ngFor="let item of recentActivity()" class="flex gap-2.5">
                <span class="mt-1.5 h-2 w-2 shrink-0 rounded-full" [ngClass]="item.tone"></span>
                <div class="min-w-0 flex-1 border-b border-slate-700/70 pb-2.5 last:border-0">
                  <div class="flex items-start justify-between gap-3">
                    <p class="text-[11px] font-semibold text-slate-100">{{ item.title }}</p>
                    <time class="shrink-0 text-[9px] text-slate-500">{{ item.date | date:'MMM d' }}</time>
                  </div>
                  <p class="mt-0.5 truncate text-[10px] text-slate-400">{{ item.description }}</p>
                </div>
              </div>
            </div>
            <ng-template #emptyActivity>
              <p class="py-10 text-center text-xs text-slate-500">Your invoices, payments, and requests will appear here.</p>
            </ng-template>
          </article>

          <div class="space-y-3">
            <article class="rounded-xl border border-slate-700 bg-[#111a31] p-4">
              <h2 class="text-xs font-semibold text-white">Quick actions</h2>
              <div class="mt-3 grid grid-cols-2 gap-2">
                <a routerLink="/tenant/payments" [queryParams]="paymentQuery()" class="flex min-h-12 items-center gap-2 rounded-lg border border-slate-700 bg-[#17223d] px-3 py-2 text-[10px] font-semibold text-slate-100 hover:border-indigo-400/50">
                  <mat-icon class="!h-4 !w-4 !text-base text-indigo-300">payments</mat-icon>Pay rent
                </a>
                <a routerLink="/tenant/requests" class="flex min-h-12 items-center gap-2 rounded-lg border border-slate-700 bg-[#17223d] px-3 py-2 text-[10px] font-semibold text-slate-100 hover:border-indigo-400/50">
                  <mat-icon class="!h-4 !w-4 !text-base text-amber-300">build</mat-icon>Report an issue
                </a>
                <a routerLink="/tenant/invoices" class="flex min-h-12 items-center gap-2 rounded-lg border border-slate-700 bg-[#17223d] px-3 py-2 text-[10px] font-semibold text-slate-100 hover:border-indigo-400/50">
                  <mat-icon class="!h-4 !w-4 !text-base text-emerald-300">receipt</mat-icon>View invoices
                </a>
                <a routerLink="/tenant/payments" class="flex min-h-12 items-center gap-2 rounded-lg border border-slate-700 bg-[#17223d] px-3 py-2 text-[10px] font-semibold text-slate-100 hover:border-indigo-400/50">
                  <mat-icon class="!h-4 !w-4 !text-base text-sky-300">history</mat-icon>Payment history
                </a>
              </div>
            </article>

            <article class="rounded-xl border border-slate-700 bg-[#111a31] p-4">
              <div class="flex items-center justify-between">
                <h2 class="text-xs font-semibold text-white">Notices</h2>
                <mat-icon class="!h-4 !w-4 !text-base text-slate-500">campaign</mat-icon>
              </div>
              <p class="mt-3 text-[10px] leading-relaxed text-slate-400">No new notices. Your property manager can share building updates here.</p>
            </article>
          </div>
        </section>
      </ng-container>
    </div>
  `
})
export class TenantPortalComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly service = inject(TenantPortalService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly invoices = signal<TenantInvoice[]>([]);
  readonly payments = signal<TenantPayment[]>([]);
  readonly requests = signal<TenantMaintenanceRequest[]>([]);
  readonly tenancy = signal<TenantTenancy | null>(null);

  readonly firstName = computed(() => this.auth.currentUser()?.firstName || 'there');
  readonly unpaidInvoices = computed(() => this.invoices()
    .filter(invoice => !['PAID', 'CANCELLED', 'VOID'].includes(invoice.status)));
  readonly dueInvoice = computed(() => [...this.unpaidInvoices()]
    .sort((a, b) => (a.dueDate || '9999-12-31').localeCompare(b.dueDate || '9999-12-31'))[0] ?? null);
  readonly monthlyRent = computed(() => this.tenancy()?.rentAmount ?? this.dueInvoice()?.amount ?? 0);
  readonly latestPayment = computed(() => [...this.payments()]
    .filter(payment => payment.status === 'COMPLETED')
    .sort((a, b) => this.paymentTimestamp(b).localeCompare(this.paymentTimestamp(a)))[0] ?? null);
  readonly openRequests = computed(() => this.requests()
    .filter(request => !['COMPLETED', 'CLOSED', 'CANCELLED', 'RESOLVED'].includes(request.status)));
  readonly leaseProgress = computed(() => {
    const tenancy = this.tenancy();
    if (!tenancy?.endDate) return 0;
    const start = new Date(tenancy.startDate).getTime();
    const end = new Date(tenancy.endDate).getTime();
    if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) return 0;
    return Math.min(100, Math.max(0, ((Date.now() - start) / (end - start)) * 100));
  });
  readonly leaseProgressLabel = computed(() => {
    const tenancy = this.tenancy();
    if (!tenancy) return 'No active lease';
    if (!tenancy.endDate) return 'Ongoing';
    const totalMonths = Math.max(1, Math.ceil((Date.parse(tenancy.endDate) - Date.parse(tenancy.startDate)) / (30 * 86400000)));
    const elapsedMonths = Math.min(totalMonths, Math.max(0, Math.floor((Date.now() - Date.parse(tenancy.startDate)) / (30 * 86400000))));
    return `${elapsedMonths} of ${totalMonths} months`;
  });
  readonly dueCountdown = computed(() => {
    const date = this.dueInvoice()?.dueDate;
    if (!date) return '';
    const days = Math.ceil((new Date(date).setHours(0, 0, 0, 0) - new Date().setHours(0, 0, 0, 0)) / 86400000);
    return days < 0 ? `${Math.abs(days)} days overdue` : `${days} days left`;
  });
  readonly recentActivity = computed<TenantActivity[]>(() => {
    const items: TenantActivity[] = [
      ...this.invoices().map(invoice => ({
        id: `invoice-${invoice.id}`,
        icon: 'receipt_long',
        title: `Invoice #${invoice.id.slice(0, 6)} ${invoice.status === 'PAID' ? 'paid' : 'issued'}`,
        description: `KES ${invoice.amount.toLocaleString()}${invoice.periodStart ? ` · ${this.invoicePeriod(invoice)}` : ''}`,
        date: invoice.createdAt || invoice.dueDate || '',
        tone: 'bg-amber-400'
      })),
      ...this.payments().map(payment => ({
        id: `payment-${payment.id}`,
        icon: 'payments',
        title: payment.status === 'COMPLETED' ? 'Payment received' : `Payment ${payment.status.toLowerCase()}`,
        description: `KES ${payment.amount.toLocaleString()}${payment.reference ? ` · ${payment.reference}` : ''}`,
        date: payment.paidAt || payment.paymentDate || payment.createdAt || '',
        tone: 'bg-emerald-400'
      })),
      ...this.requests().map(request => ({
        id: `request-${request.id}`,
        icon: 'build',
        title: `Request ${request.status.toLowerCase().replace('_', ' ')}`,
        description: request.title,
        date: request.requestedDate || request.createdAt || '',
        tone: 'bg-indigo-400'
      }))
    ];
    return items.filter(item => item.date).sort((a, b) => b.date.localeCompare(a.date)).slice(0, 4);
  });

  ngOnInit(): void {
    const tenantId = this.auth.currentUser()?.tenantId;
    if (!tenantId) {
      this.error.set('Your tenant profile is not linked to the system yet.');
      this.loading.set(false);
      return;
    }

    forkJoin({
      invoices: this.service.getInvoices(tenantId, 0, 100),
      payments: this.service.getPayments(tenantId, 0, 100),
      requests: this.service.getMaintenanceRequests(tenantId, 0, 100),
      tenancy: this.service.getMyActiveTenancy()
    }).subscribe({
      next: response => {
        this.invoices.set(response.invoices.data?.content ?? []);
        this.payments.set(response.payments.data?.content ?? []);
        this.requests.set(response.requests.data?.content ?? []);
        this.tenancy.set(response.tenancy.data ?? null);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load your dashboard right now. Please try again later.');
        this.loading.set(false);
      }
    });
  }

  paymentQuery(): Record<string, string> {
    const invoice = this.dueInvoice();
    return {
      paymentType: 'rent',
      ...(invoice ? {
        invoiceId: invoice.id,
        amount: String(invoice.amount),
        unitId: invoice.unitId || this.tenancy()?.unitId || '',
        reference: `Invoice ${invoice.id.slice(0, 8)}`
      } : {})
    };
  }

  securityDepositQuery(): Record<string, string> {
    const depositAmount = this.tenancy()?.securityDepositAmount;
    return {
      paymentType: 'security-deposit',
      ...(depositAmount && depositAmount > 0 ? { amount: String(depositAmount) } : {}),
      ...(this.tenancy()?.unitId ? { unitId: this.tenancy()!.unitId } : {})
    };
  }

  invoicePeriod(invoice: TenantInvoice): string {
    if (!invoice.periodStart) return 'Rent invoice';
    return new Date(invoice.periodStart).toLocaleString('en', { month: 'long' }) + ' rent';
  }

  private paymentTimestamp(payment: TenantPayment): string {
    return payment.paidAt || payment.paymentDate || payment.createdAt || '';
  }
}