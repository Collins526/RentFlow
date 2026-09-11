import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantInvoice, TenantPortalService } from '../../core/services/tenant/tenant-portal.service';

@Component({
  selector: 'app-tenant-invoices',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="space-y-6 p-6">
      <div class="flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-medium uppercase tracking-wide text-indigo-600">Tenant portal</p>
          <h1 class="mt-1 text-2xl font-semibold text-slate-900">Invoices</h1>
        </div>
        <a mat-stroked-button routerLink="/tenant">
          <mat-icon>arrow_back</mat-icon>
          Back to portal
        </a>
      </div>

      <div *ngIf="loading()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">
        Loading invoices…
      </div>

      <div *ngIf="error()" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
        {{ error() }}
      </div>

      <div *ngIf="!loading() && !error()" class="space-y-3">
        <div *ngIf="items().length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-slate-500">
          No invoices are available for your account yet.
        </div>

        <div *ngFor="let item of items()" class="rounded-2xl border border-slate-200 bg-white shadow-sm transition-shadow hover:shadow-md">
          <button type="button" class="w-full cursor-pointer p-4 text-left" (click)="toggleDetails(item.id)" [attr.aria-expanded]="expandedInvoiceId() === item.id">
            <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm text-slate-500">Invoice #{{ item.id.slice(0, 8) }}</p>
              <h2 class="text-lg font-semibold text-slate-900">{{ item.tenantName ?? 'Rent invoice' }}</h2>
            </div>
            <div class="text-right">
              <p class="text-xl font-bold text-slate-900">{{ item.amount | currency:'KES ':'symbol':'1.0-2' }}</p>
              <span class="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">{{ invoiceStatusLabel(item.status) }}</span>
            </div>
            </div>
            <div class="mt-3 flex flex-wrap items-center gap-4 text-sm text-slate-500">
              <span>Unit: {{ item.unitNumber ?? 'Not specified' }}</span>
              <span>Billing month: {{ (item.periodStart || item.periodEnd) | date:'MMMM yyyy' }}</span>
              <span>Deadline: {{ (item.dueDate || item.periodEnd) | date:'mediumDate' }}</span>
            </div>
          </button>
          <div *ngIf="expandedInvoiceId() === item.id" class="border-t border-slate-200 bg-slate-50 p-4">
            <div class="grid gap-3 text-sm text-slate-600 sm:grid-cols-2">
              <p><span class="font-semibold text-slate-900">Tenant:</span> {{ item.tenantName ?? 'Not specified' }}</p>
              <p><span class="font-semibold text-slate-900">Unit:</span> {{ item.unitNumber ?? 'Not specified' }}</p>
              <p><span class="font-semibold text-slate-900">Billing month:</span> {{ (item.periodStart || item.periodEnd) | date:'MMMM yyyy' }}</p>
              <p><span class="font-semibold text-slate-900">Amount:</span> {{ item.amount | currency:'KES ':'symbol':'1.0-2' }}</p>
              <p><span class="font-semibold text-slate-900">Payment deadline:</span> {{ (item.dueDate || item.periodEnd) | date:'mediumDate' }}</p>
              <p><span class="font-semibold text-slate-900">Status:</span> {{ invoiceStatusLabel(item.status) }}</p>
            </div>
            <div *ngIf="item.notes" class="mt-3 rounded-lg border border-slate-200 bg-white p-3 text-sm text-slate-600">{{ item.notes }}</div>
            <div class="mt-4 flex justify-end">
              <button mat-flat-button color="primary" type="button" (click)="payInvoice(item); $event.stopPropagation()" [disabled]="item.status === 'PAID' || item.status === 'SETTLED'">
                <mat-icon>payments</mat-icon>
                {{ item.status === 'PAID' || item.status === 'SETTLED' ? 'Settled' : 'Pay invoice' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  providers: [CurrencyPipe, DatePipe]
})
export class TenantInvoicesComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly service = inject(TenantPortalService);
  private readonly router = inject(Router);

  items = signal<TenantInvoice[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  expandedInvoiceId = signal<string | null>(null);

  invoiceStatusLabel(status: string): string { return status === 'PAID' ? 'SETTLED' : status; }

  toggleDetails(invoiceId: string): void {
    this.expandedInvoiceId.update(current => current === invoiceId ? null : invoiceId);
  }

  payInvoice(invoice: TenantInvoice): void {
    this.router.navigate(['/tenant/payments'], {
      queryParams: {
        invoiceId: invoice.id,
        amount: invoice.amount,
        unitId: invoice.unitId ?? '',
        reference: `Invoice ${invoice.id.slice(0, 8)}`
      }
    });
  }

  ngOnInit(): void {
    const tenantId = this.auth.currentUser()?.tenantId;
    if (!tenantId) {
      this.error.set('Your tenant profile is not linked to the system yet.');
      this.loading.set(false);
      return;
    }

    this.service.getInvoices(tenantId, 0, 10).subscribe({
      next: res => {
        this.items.set(res.data?.content ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load your invoices right now.');
        this.loading.set(false);
      }
    });
  }
}
