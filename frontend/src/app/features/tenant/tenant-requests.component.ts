import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CreateTenantMaintenanceRequest, TenantMaintenanceRequest, TenantPortalService } from '../../core/services/tenant/tenant-portal.service';

@Component({
  selector: 'app-tenant-requests',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule],
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
        <div class="rounded-2xl border border-indigo-200 bg-indigo-50 p-5 shadow-sm">
          <div class="mb-4">
            <p class="text-sm font-semibold uppercase tracking-wide text-indigo-700">Report an issue</p>
            <h2 class="mt-1 text-xl font-semibold text-slate-900">Submit a maintenance request</h2>
            <p class="mt-1 text-sm text-slate-600">Tell your property manager what needs attention.</p>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Issue title</mat-label>
              <input matInput [(ngModel)]="title" name="title" maxlength="120" placeholder="Leaking kitchen tap" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Priority</mat-label>
              <mat-select [(ngModel)]="priority" name="priority">
                <mat-option value="LOW">Low</mat-option>
                <mat-option value="MEDIUM">Medium</mat-option>
                <mat-option value="HIGH">High</mat-option>
                <mat-option value="CRITICAL">Critical</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full md:col-span-2">
              <mat-label>Describe the problem</mat-label>
              <textarea matInput [(ngModel)]="description" name="description" rows="4" maxlength="1000" placeholder="Add details that will help us resolve the issue."></textarea>
            </mat-form-field>

            <div class="w-full md:col-span-2">
              <label class="mb-2 block text-sm font-medium text-slate-700" for="maintenance-attachment">Photo or video</label>
              <input id="maintenance-attachment" type="file" accept="image/*,video/*" (change)="onAttachmentSelected($event)" class="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 file:mr-3 file:rounded-md file:border-0 file:bg-indigo-50 file:px-3 file:py-2 file:font-medium file:text-indigo-700" />
              <p class="mt-1 text-xs text-slate-500">Optional. Images and videos up to 10 MB.</p>
              <p *ngIf="attachmentName" class="mt-2 text-sm text-slate-600">Attached: {{ attachmentName }}</p>
            </div>
          </div>

          <div *ngIf="submitMessage()" class="mt-2 rounded-xl border px-3 py-3 text-sm" [ngClass]="submitSuccess() ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'">
            {{ submitMessage() }}
          </div>

          <div class="mt-4 flex justify-end">
            <button mat-flat-button color="primary" type="button" [disabled]="submitting()" (click)="submitRequest()">
              {{ submitting() ? 'Submitting…' : 'Submit request' }}
            </button>
          </div>
        </div>

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
          <a
            *ngIf="item.attachmentData"
            [href]="item.attachmentData"
            [download]="item.attachmentName || 'maintenance-attachment'"
            target="_blank"
            rel="noopener"
            class="mt-3 inline-flex items-center gap-1 text-sm font-medium text-indigo-700 hover:text-indigo-900"
          >
            <mat-icon class="text-base">attach_file</mat-icon>
            View attachment{{ item.attachmentName ? ': ' + item.attachmentName : '' }}
          </a>
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
  submitting = signal(false);
  submitMessage = signal<string | null>(null);
  submitSuccess = signal(false);

  title = '';
  description = '';
  priority: CreateTenantMaintenanceRequest['priority'] = 'MEDIUM';
  attachmentData: string | null = null;
  attachmentName: string | null = null;
  attachmentType: string | null = null;
  attachmentSize: number | null = null;

  onAttachmentSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.submitMessage.set(null);

    if (!file) {
      this.clearAttachment();
      return;
    }

    if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
      input.value = '';
      this.clearAttachment();
      this.submitSuccess.set(false);
      this.submitMessage.set('Please attach an image or video file.');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      input.value = '';
      this.clearAttachment();
      this.submitSuccess.set(false);
      this.submitMessage.set('Attachments must be 10 MB or smaller.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.attachmentData = typeof reader.result === 'string' ? reader.result : null;
      this.attachmentName = file.name;
      this.attachmentType = file.type;
      this.attachmentSize = file.size;
    };
    reader.readAsDataURL(file);
  }

  private clearAttachment(): void {
    this.attachmentData = null;
    this.attachmentName = null;
    this.attachmentType = null;
    this.attachmentSize = null;
  }

  submitRequest(): void {
    const user = this.auth.currentUser();
    const title = this.title.trim();

    if (!user?.tenantId) {
      this.submitSuccess.set(false);
      this.submitMessage.set('Your tenant profile is not linked to the system yet.');
      return;
    }

    if (!title) {
      this.submitSuccess.set(false);
      this.submitMessage.set('Please enter a title for the maintenance issue.');
      return;
    }

    const request: CreateTenantMaintenanceRequest = {
      tenantId: user.tenantId,
      unitId: user.unitId ?? null,
      title,
      description: this.description.trim() || null,
      attachmentData: this.attachmentData,
      attachmentName: this.attachmentName,
      attachmentType: this.attachmentType,
      attachmentSize: this.attachmentSize,
      priority: this.priority,
      status: 'REQUESTED',
      requestedDate: new Date().toISOString().slice(0, 10)
    };

    this.submitting.set(true);
    this.submitMessage.set(null);
    this.service.createMaintenanceRequest(request).subscribe({
      next: () => {
        this.submitting.set(false);
        this.submitSuccess.set(true);
        this.submitMessage.set('Maintenance request submitted successfully.');
        this.title = '';
        this.description = '';
        this.priority = 'MEDIUM';
        this.clearAttachment();
        this.loadRequests();
      },
      error: err => {
        this.submitting.set(false);
        this.submitSuccess.set(false);
        this.submitMessage.set(err?.error?.message || 'Unable to submit the maintenance request right now.');
      }
    });
  }

  ngOnInit(): void {
    this.loadRequests();
  }

  private loadRequests(): void {
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
