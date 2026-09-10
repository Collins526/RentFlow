import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { ActivatedRoute } from '@angular/router';
import { TenancyService } from '../../../core/services/tenancy/tenancy.service';
import { ToastService, resolveApiMessage } from '../../../core/services/toast.service';
import { formatEnumLabel } from '../../../core/services/unit/unit.service';
import {
  Tenancy,
  TenancyStatus,
  TENANCY_STATUSES,
  isTenancyOpen
} from '../../../core/models/tenancy.model';
import { TenancyForm } from '../tenancy-form/tenancy-form';
import { TenancyEndDialog } from '../tenancy-end-dialog/tenancy-end-dialog';
import {
  ConfirmDialogComponent,
  DataTableCellDirective,
  DataTableColumn,
  DataTableComponent,
  FilterBarComponent,
  PageHeaderComponent,
  StatusChipComponent,
  statusTone
} from '../../../shared/ui';

@Component({
  selector: 'app-tenancy-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatMenuModule,
    PageHeaderComponent,
    FilterBarComponent,
    DataTableComponent,
    DataTableCellDirective,
    StatusChipComponent
  ],
  providers: [CurrencyPipe, DatePipe],
  template: `
    <app-page-header
      title="Tenancies"
      subtitle="Manage tenant-unit assignments and occupancy.">
      <button *ngIf="!organizationId" actions mat-flat-button color="primary" (click)="openTenancyForm()">
        <mat-icon>add</mat-icon>
        New Tenancy
      </button>
    </app-page-header>

    <app-filter-bar
      [showClear]="!!statusFilter"
      [resultLabel]="resultLabel()"
      (clear)="clearFilters()">
      <mat-form-field appearance="outline" class="w-56" subscriptSizing="dynamic">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="statusFilter" (selectionChange)="applyFilter()">
          <mat-option [value]="null">All statuses</mat-option>
          <mat-option *ngFor="let status of statuses" [value]="status">
            {{ formatLabel(status) }}
          </mat-option>
        </mat-select>
      </mat-form-field>
    </app-filter-bar>

    <app-data-table
      [columns]="columns"
      [rows]="tenancies()"
      [loading]="isLoading()"
      loadingLabel="Loading tenancies…"
      [error]="loadError()"
      emptyIcon="assignment_ind"
      [emptyTitle]="emptyTitle()"
      [emptyMessage]="emptyMessage()"
      [totalElements]="totalElements()"
      [pageSize]="pageSize()"
      [pageIndex]="pageIndex()"
      (page)="handlePageEvent($event)"
      (retry)="loadTenancies()">

      <ng-template rfCell="dates" let-tenancy>
        <div class="text-gray-600">
          {{ tenancy.startDate | date:'mediumDate' }}
          <div class="text-xs text-gray-400">
            to {{ tenancy.endDate ? (tenancy.endDate | date:'mediumDate') : 'Ongoing' }}
          </div>
        </div>
      </ng-template>

      <ng-template rfCell="status" let-tenancy>
        <app-status-chip
          [label]="formatLabel(tenancy.status)"
          [tone]="statusTone(tenancy.status)">
        </app-status-chip>
      </ng-template>

      <ng-template rfCell="actions" let-tenancy>
        <button mat-icon-button [matMenuTriggerFor]="menu" (click)="$event.stopPropagation()"
                aria-label="Tenancy actions">
          <mat-icon>more_vert</mat-icon>
        </button>
        <mat-menu #menu="matMenu">
          <button mat-menu-item (click)="openTenancyForm(tenancy)">
            <mat-icon>edit</mat-icon>
            <span>Edit</span>
          </button>
          <button mat-menu-item (click)="openEndDialog(tenancy)" [disabled]="!canEnd(tenancy)">
            <mat-icon>event_busy</mat-icon>
            <span>End tenancy</span>
          </button>
          <button mat-menu-item (click)="deleteTenancy(tenancy)">
            <mat-icon color="warn">delete</mat-icon>
            <span>Delete</span>
          </button>
        </mat-menu>
      </ng-template>
    </app-data-table>
  `
})
export class TenancyList implements OnInit {
  private tenancyService = inject(TenancyService);
  private dialog = inject(MatDialog);
  private toast = inject(ToastService);
  private currency = inject(CurrencyPipe);
  private route = inject(ActivatedRoute);

  readonly statuses = TENANCY_STATUSES;
  readonly formatLabel = formatEnumLabel;
  readonly statusTone = statusTone;

  tenancies = signal<Tenancy[]>([]);
  isLoading = signal(true);
  loadError = signal<string | null>(null);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  organizationId = this.route.snapshot.queryParamMap.get('organizationId') ?? undefined;

  statusFilter: TenancyStatus | null = null;

  readonly columns: DataTableColumn<Tenancy>[] = [
    {
      key: 'tenantName',
      header: 'Tenant',
      value: row => row.tenantName,
      cellClass: 'font-medium text-gray-900'
    },
    { key: 'unitNumber', header: 'Unit', value: row => row.unitNumber, cellClass: 'text-gray-700' },
    { key: 'dates', header: 'Period' },
    {
      key: 'rentAmount',
      header: 'Rent',
      value: row => this.currency.transform(row.rentAmount ?? 0, 'KES ', 'symbol', '1.0-2'),
      cellClass: 'text-gray-700 font-medium'
    },
    { key: 'status', header: 'Status' },
    { key: 'actions', header: '', align: 'right' }
  ];

  resultLabel = computed(() => {
    const total = this.totalElements();
    return this.isLoading() ? '' : `${total} ${total === 1 ? 'tenancy' : 'tenancies'}`;
  });

  emptyTitle = computed(() =>
    this.statusFilter ? `No ${this.formatLabel(this.statusFilter).toLowerCase()} tenancies` : 'No tenancies yet');

  emptyMessage = computed(() =>
    this.statusFilter
      ? 'Try a different status filter.'
      : 'Create a tenancy to place a tenant in a unit and start tracking occupancy.');

  ngOnInit(): void {
    this.loadTenancies();
  }

  loadTenancies(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.tenancyService
      .getTenancies(this.pageIndex(), this.pageSize(), {
        status: this.statusFilter,
        organizationId: this.organizationId
      })
      .subscribe({
        next: res => {
          this.tenancies.set(res.data?.content ?? []);
          this.totalElements.set(res.data?.totalElements ?? 0);
          this.isLoading.set(false);
        },
        error: err => {
          this.tenancies.set([]);
          this.totalElements.set(0);
          this.loadError.set(resolveApiMessage(err, 'Could not load tenancies.'));
          this.isLoading.set(false);
        }
      });
  }

  applyFilter(): void {
    this.pageIndex.set(0);
    this.loadTenancies();
  }

  clearFilters(): void {
    this.statusFilter = null;
    this.applyFilter();
  }

  handlePageEvent(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTenancies();
  }

  canEnd(tenancy: Tenancy): boolean {
    return isTenancyOpen(tenancy.status);
  }

  openTenancyForm(tenancy?: Tenancy): void {
    const dialogRef = this.dialog.open(TenancyForm, {
      width: '680px',
      data: { tenancy },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toast.success(tenancy ? 'Tenancy updated.' : 'Tenancy created.');
        this.loadTenancies();
      }
    });
  }

  openEndDialog(tenancy: Tenancy): void {
    const dialogRef = this.dialog.open(TenancyEndDialog, {
      width: '480px',
      data: { tenancy },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toast.success(`Tenancy ended. Unit ${tenancy.unitNumber} is now vacant.`);
        this.loadTenancies();
      }
    });
  }

  deleteTenancy(tenancy: Tenancy): void {
    const confirmRef = this.dialog.open(ConfirmDialogComponent, {
      width: '440px',
      data: {
        title: 'Delete tenancy?',
        message: `${tenancy.tenantName}'s tenancy of unit ${tenancy.unitNumber} will be archived `
          + 'rather than erased, so rent history stays intact. The unit is released if it was occupied.',
        confirmLabel: 'Delete',
        destructive: true
      }
    });

    confirmRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.tenancyService.deleteTenancy(tenancy.id).subscribe({
        next: () => {
          this.toast.success('Tenancy deleted.');
          this.loadTenancies();
        },
        error: err => this.toast.apiError(err, 'Could not delete the tenancy.')
      });
    });
  }
}
