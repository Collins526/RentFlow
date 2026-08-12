import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantMaintenanceRequest, TenantPortalService } from '../../core/services/tenant/tenant-portal.service';

@Component({
  selector: 'app-tenant-requests',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="space-y-6 p-6">
      <div class="flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-medium uppercase tracking-wide text-indigo-600">Tenant portal</p>
          <h1 class="mt-1 text-2xl font-semibold text-slate-900">Maintenance requests</h1>
        </div>
        <a mat-stroked-button routerLink="/tenant">
          <mat-icon>arrow_back</mat-icon>
          Back to portal
        </a>
      </div>

      <div *ngIf="loading()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">
        Loading requests…
      </div>

      <div *ngIf="error()" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
        {{ error() }}
      </div>

      <div *ngIf="!loading() && !error()" class="space-y-3">
        <div *ngIf="items().length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-slate-500">
          You have not submitted any maintenance requests yet.
        </div>

        <div *ngFor="let item of items()" class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-sm text-slate-500">{{ item.priority || 'Normal priority' }}</p>
              <h2 class="text-lg font-semibold text-slate-900">{{ item.title }}</h2>
            </div>
            <span class="rounded-full bg-amber-50 px-2 py-1 text-xs font-medium text-amber-700">{{ item.status }}</span>
          </div>
          <p *ngIf="item.description" class="mt-3 text-sm text-slate-600">{{ item.description }}</p>
          <div class="mt-3 text-sm text-slate-500">
            Submitted {{ (item.requestedDate || item.id) | date:'mediumDate' }}
          </div>
        </div>
      </div>
    </div>
  `,
  providers: [DatePipe]
})
export class TenantRequestsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly service = inject(TenantPortalService);

  items = signal<TenantMaintenanceRequest[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const tenantId = this.auth.currentUser()?.tenantId;
    if (!tenantId) {
      this.error.set('Your tenant profile is not linked to the system yet.');
      this.loading.set(false);
      return;
    }

    this.service.getMaintenanceRequests(tenantId, 0, 10).subscribe({
      next: res => {
        this.items.set(res.data?.content ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load your maintenance requests right now.');
        this.loading.set(false);
      }
    });
  }
}
