import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { MaintenanceRequestItem, MaintenanceService } from '../../core/services/maintenance/maintenance.service';
import { ToastService } from '../../core/services/toast.service';
import { PageHeaderComponent, StatusChipComponent, statusTone } from '../../shared/ui';

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule, PageHeaderComponent, StatusChipComponent],
  template: `
    <app-page-header title="Maintenance requests" subtitle="Review tenant issues and take action on outstanding work.">
      <a actions mat-stroked-button routerLink="/dashboard"><mat-icon>arrow_back</mat-icon> Dashboard</a>
    </app-page-header>

    <div *ngIf="error()" class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-rose-700">{{ error() }}</div>
    <div *ngIf="loading()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">Loading maintenance requests…</div>
    <div *ngIf="!loading() && requests().length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-slate-500">No tenant maintenance requests yet.</div>

    <div *ngFor="let request of requests()" class="mb-3 rounded-2xl border border-slate-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      <button
        type="button"
        class="w-full cursor-pointer p-5 text-left"
        (click)="toggleDetails(request.id)"
        [attr.aria-expanded]="expandedRequestId() === request.id"
      >
        <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p class="text-xs font-semibold uppercase tracking-wide text-indigo-600">{{ request.priority }} priority</p>
          <h2 class="mt-1 text-lg font-semibold text-slate-900">{{ request.title }}</h2>
          <p class="mt-1 text-sm text-slate-500">Tenant: {{ request.tenantName || request.tenantId }} · Unit: {{ request.unitNumber || request.unitId || 'Not specified' }}</p>
        </div>
        <app-status-chip [label]="request.status" [tone]="statusTone(request.status)"></app-status-chip>
        </div>
        <p *ngIf="request.description" class="mt-4 line-clamp-2 text-sm text-slate-700">{{ request.description }}</p>
        <div class="mt-4 flex flex-wrap gap-4 text-sm text-slate-500">
          <span>Submitted {{ request.requestedDate | date:'mediumDate' }}</span>
          <span *ngIf="request.assignedTo">Assigned to {{ request.assignedTo }}</span>
          <span class="font-medium text-indigo-700">{{ expandedRequestId() === request.id ? 'Hide details' : 'View details' }}</span>
        </div>
      </button>

      <div *ngIf="expandedRequestId() === request.id" class="border-t border-slate-200 bg-slate-50 p-5">
        <div class="grid gap-3 text-sm text-slate-600 sm:grid-cols-2">
          <p><span class="font-semibold text-slate-900">Tenant:</span> {{ request.tenantName || request.tenantId }}</p>
          <p><span class="font-semibold text-slate-900">Unit:</span> {{ request.unitNumber || request.unitId || 'Not specified' }}</p>
          <p><span class="font-semibold text-slate-900">Priority:</span> {{ request.priority }}</p>
          <p><span class="font-semibold text-slate-900">Status:</span> {{ request.status }}</p>
          <p><span class="font-semibold text-slate-900">Submitted:</span> {{ request.requestedDate | date:'mediumDate' }}</p>
          <p><span class="font-semibold text-slate-900">Scheduled:</span> {{ request.scheduledDate ? (request.scheduledDate | date:'mediumDate') : 'Not scheduled' }}</p>
          <p><span class="font-semibold text-slate-900">Completed:</span> {{ request.completedDate ? (request.completedDate | date:'mediumDate') : 'Not completed' }}</p>
          <p><span class="font-semibold text-slate-900">Assigned to:</span> {{ request.assignedTo || 'Not assigned' }}</p>
        </div>
        <p *ngIf="request.description" class="mt-4 rounded-lg border border-slate-200 bg-white p-3 text-sm text-slate-700">{{ request.description }}</p>
        <a
          *ngIf="request.attachmentData"
          [href]="request.attachmentData"
          [download]="request.attachmentName || 'maintenance-attachment'"
          target="_blank"
          rel="noopener"
          class="mt-4 inline-flex items-center gap-1 text-sm font-medium text-indigo-700"
          (click)="$event.stopPropagation()"
        >
          <mat-icon>attach_file</mat-icon> View attachment{{ request.attachmentName ? ': ' + request.attachmentName : '' }}
        </a>
      </div>
    </div>
  `,
  providers: [DatePipe]
})
export class MaintenanceListComponent implements OnInit {
  private readonly service = inject(MaintenanceService);
  private readonly toast = inject(ToastService);
  requests = signal<MaintenanceRequestItem[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  expandedRequestId = signal<string | null>(null);
  readonly statusTone = statusTone;

  toggleDetails(requestId: string): void {
    this.expandedRequestId.update(current => current === requestId ? null : requestId);
  }

  ngOnInit(): void {
    this.service.getRequests().subscribe({
      next: response => { this.requests.set(response.data?.content ?? []); this.loading.set(false); },
      error: () => { this.error.set('Unable to load tenant maintenance requests.'); this.loading.set(false); }
    });
  }
}
