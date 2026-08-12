import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
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

        <div *ngFor="let item of items()" class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm text-slate-500">Invoice #{{ item.id.slice(0, 8) }}</p>
              <h2 class="text-lg font-semibold text-slate-900">{{ item.unitNumber ?? 'Unit invoice' }}</h2>
            </div>
            <div class="text-right">
              <p class="text-xl font-bold text-slate-900">{{ item.amount | currency:'KES ':'symbol':'1.0-2' }}</p>
              <span class="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">{{ item.status }}</span>
            </div>
          </div>
          <div class="mt-3 flex flex-wrap items-center gap-4 text-sm text-slate-500">
            <span>Due: {{ (item.dueDate || item.periodEnd) | date:'mediumDate' }}</span>
            <span>Period end: {{ (item.periodEnd || item.dueDate) | date:'mediumDate' }}</span>
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

  items = signal<TenantInvoice[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

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
