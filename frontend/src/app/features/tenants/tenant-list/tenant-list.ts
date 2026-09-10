import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { ActivatedRoute } from '@angular/router';
import { TenantService } from '../../../core/services/tenant/tenant.service';
import { ToastService, resolveApiMessage } from '../../../core/services/toast.service';
import { formatEnumLabel } from '../../../core/services/unit/unit.service';
import { Tenant, TenantType } from '../../../core/models/tenant.model';
import { TenantForm } from '../tenant-form/tenant-form';
import { TenancyForm } from '../../tenancies/tenancy-form/tenancy-form';
import {
  ConfirmDialogComponent,
  DataTableCellDirective,
  DataTableColumn,
  DataTableComponent,
  PageHeaderComponent,
  StatusChipComponent,
  statusTone
} from '../../../shared/ui';

@Component({
  selector: 'app-tenant-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    PageHeaderComponent,
    DataTableComponent,
    DataTableCellDirective,
    StatusChipComponent
  ],
  template: `
    <app-page-header
      title="Tenants"
      [subtitle]="subtitle()">
      <button *ngIf="!organizationId" actions mat-flat-button color="primary" (click)="openTenantForm()">
        <mat-icon>add</mat-icon>
        Add Tenant
      </button>
    </app-page-header>

    <app-data-table
      [columns]="columns"
      [rows]="tenants()"
      [loading]="isLoading()"
      loadingLabel="Loading tenants…"
      [error]="loadError()"
      emptyIcon="people"
      emptyTitle="No tenants yet"
      emptyMessage="Add a tenant before placing them in a unit."
      [totalElements]="totalElements()"
      [pageSize]="pageSize()"
      [pageIndex]="pageIndex()"
      (page)="handlePageEvent($event)"
      (retry)="loadTenants()">

      <ng-template rfCell="type" let-tenant>
        <app-status-chip [label]="formatLabel(tenant.tenantType)" tone="neutral"></app-status-chip>
      </ng-template>

      <ng-template rfCell="contact" let-tenant>
        <div class="text-gray-600">
          {{ tenant.email }}
          <div class="text-xs text-gray-400">{{ tenant.phoneNumber }}</div>
        </div>
      </ng-template>

      <ng-template rfCell="status" let-tenant>
        <app-status-chip
          [label]="formatLabel(tenant.status)"
          [tone]="statusTone(tenant.status)">
        </app-status-chip>
      </ng-template>

      <ng-template rfCell="actions" let-tenant>
        <button mat-icon-button [matMenuTriggerFor]="menu" (click)="$event.stopPropagation()"
                aria-label="Tenant actions">
          <mat-icon>more_vert</mat-icon>
        </button>
        <mat-menu #menu="matMenu">
          <button mat-menu-item (click)="openTenantForm(tenant)">
            <mat-icon>edit</mat-icon>
            <span>Edit</span>
          </button>
          <button mat-menu-item (click)="openTenancyFormForTenant(tenant)">
            <mat-icon>apartment</mat-icon>
            <span>Assign unit</span>
          </button>
          <button mat-menu-item (click)="deactivateTenant(tenant)">
            <mat-icon color="warn">person_off</mat-icon>
            <span>Deactivate</span>
          </button>
        </mat-menu>
      </ng-template>
    </app-data-table>
  `
})
export class TenantList implements OnInit {
  private tenantService = inject(TenantService);
  private dialog = inject(MatDialog);
  private toast = inject(ToastService);
  private route = inject(ActivatedRoute);

  readonly formatLabel = formatEnumLabel;
  readonly statusTone = statusTone;

  tenants = signal<Tenant[]>([]);
  isLoading = signal(true);
  loadError = signal<string | null>(null);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  organizationId = this.route.snapshot.queryParamMap.get('organizationId') ?? undefined;

  readonly columns: DataTableColumn<Tenant>[] = [
    {
      key: 'name',
      header: 'Name',
      value: row => this.tenantName(row),
      cellClass: 'font-medium text-gray-900'
    },
    { key: 'type', header: 'Type' },
    { key: 'contact', header: 'Contact' },
    { key: 'status', header: 'Status' },
    { key: 'actions', header: '', align: 'right' }
  ];

  subtitle = computed(() => {
    const total = this.totalElements();
    return this.isLoading()
      ? "Manage your organization's tenants."
      : `${total} ${total === 1 ? 'tenant' : 'tenants'} in your organization.`;
  });

  ngOnInit(): void {
    this.loadTenants();
  }

  tenantName(tenant: Tenant): string {
    return tenant.tenantType === TenantType.CORPORATE
      ? tenant.companyName ?? 'Unnamed company'
      : `${tenant.firstName ?? ''} ${tenant.lastName ?? ''}`.trim() || 'Unnamed tenant';
  }

  loadTenants(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.tenantService.getAllTenants(this.pageIndex(), this.pageSize(), this.organizationId).subscribe({
      next: res => {
        this.tenants.set(res.data?.content ?? []);
        this.totalElements.set(res.data?.totalElements ?? 0);
        this.isLoading.set(false);
      },
      error: err => {
        this.tenants.set([]);
        this.totalElements.set(0);
        this.loadError.set(resolveApiMessage(err, 'Could not load tenants.'));
        this.isLoading.set(false);
      }
    });
  }

  handlePageEvent(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTenants();
  }

  openTenantForm(tenant?: Tenant): void {
    const dialogRef = this.dialog.open(TenantForm, {
      width: '600px',
      data: { tenant },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toast.success(tenant ? 'Tenant updated.' : 'Tenant added.');
        this.loadTenants();
        if (!tenant) {
          this.openTenancyFormForTenant(result);
        }
      }
    });
  }
  openTenancyFormForTenant(tenant: Tenant): void {
    const dialogRef = this.dialog.open(TenancyForm, {
      width: '680px',
      data: { tenant },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toast.success('Unit assigned to tenant.');
        this.loadTenants();
      }
    });
  }
  deactivateTenant(tenant: Tenant): void {
    const confirmRef = this.dialog.open(ConfirmDialogComponent, {
      width: '440px',
      data: {
        title: 'Deactivate tenant?',
        message: `${this.tenantName(tenant)} will be marked inactive. Their record and any `
          + 'tenancy history are kept.',
        confirmLabel: 'Deactivate',
        destructive: true
      }
    });

    confirmRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.tenantService.deleteTenant(tenant.id).subscribe({
        next: () => {
          this.toast.success('Tenant deactivated.');
          this.loadTenants();
        },
        error: err => this.toast.apiError(err, 'Could not deactivate the tenant.')
      });
    });
  }
}
