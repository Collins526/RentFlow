import { Component, Inject, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TenancyService } from '../../../core/services/tenancy/tenancy.service';
import { TenantService } from '../../../core/services/tenant/tenant.service';
import { resolveApiMessage } from '../../../core/services/toast.service';
import { PropertyService, Property } from '../../../core/services/property/property.service';
import { UnitService, Unit, formatEnumLabel } from '../../../core/services/unit/unit.service';
import { Tenancy, TenancyRequest, TenancyStatus, TENANCY_STATUSES } from '../../../core/models/tenancy.model';
import { Tenant, TenantType } from '../../../core/models/tenant.model';
import { TenantCredentialsDialog } from '../tenant-credentials-dialog/tenant-credentials-dialog';

@Component({
  selector: 'app-tenancy-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    TenantCredentialsDialog
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit() ? 'Edit Tenancy' : 'Create Tenancy' }}</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 mt-4">

        <div *ngIf="serverError()"
             class="flex items-start gap-2 rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-800">
          <mat-icon class="!text-lg !h-5 !w-5">error_outline</mat-icon>
          <span>{{ serverError() }}</span>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Tenant</mat-label>
          <mat-select formControlName="tenantId">
            <mat-option *ngFor="let t of tenants()" [value]="t.id">
              {{ tenantLabel(t) }}
            </mat-option>
          </mat-select>
          <mat-hint *ngIf="!isLoadingTenants() && tenants().length === 0">
            No tenants yet — add one from the Tenants page first.
          </mat-hint>
          <mat-error *ngIf="form.get('tenantId')?.hasError('required')">Tenant is required</mat-error>
        </mat-form-field>

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Property</mat-label>
            <mat-select formControlName="propertyId" (selectionChange)="onPropertyChange($event.value)">
              <mat-option *ngFor="let p of properties()" [value]="p.id">{{ p.name }}</mat-option>
            </mat-select>
            <mat-error *ngIf="form.get('propertyId')?.hasError('required')">Property is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Unit</mat-label>
            <mat-select formControlName="unitId">
              <mat-option *ngFor="let u of units()" [value]="u.id" [disabled]="isUnitUnavailable(u)">
                {{ u.unitNumber }}
                <span class="text-xs text-gray-500">
                  &middot; {{ formatLabel(u.type) }} &middot; {{ formatLabel(u.occupancyStatus) }}
                </span>
              </mat-option>
            </mat-select>
            <mat-hint *ngIf="form.get('propertyId')?.value && !isLoadingUnits() && units().length === 0">
              This property has no created units. Add a unit before creating a tenancy.
              <button mat-button type="button" class="!px-1 !min-w-0" (click)="openUnitManager()">
                Manage units
              </button>
            </mat-hint>
            <mat-hint *ngIf="!form.get('propertyId')?.value">Pick a property first.</mat-hint>
            <mat-error *ngIf="form.get('unitId')?.hasError('required')">Unit is required</mat-error>
          </mat-form-field>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Start Date</mat-label>
            <input matInput [matDatepicker]="startPicker" formControlName="startDate">
            <mat-datepicker-toggle matIconSuffix [for]="startPicker"></mat-datepicker-toggle>
            <mat-datepicker #startPicker></mat-datepicker>
            <mat-error *ngIf="form.get('startDate')?.hasError('required')">Start date is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>End Date</mat-label>
            <input matInput [matDatepicker]="endPicker" formControlName="endDate">
            <mat-datepicker-toggle matIconSuffix [for]="endPicker"></mat-datepicker-toggle>
            <mat-datepicker #endPicker></mat-datepicker>
            <mat-hint>Leave blank for month to month</mat-hint>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option *ngFor="let s of statuses" [value]="s">{{ formatLabel(s) }}</mat-option>
          </mat-select>
          <mat-hint>{{ statusHint() }}</mat-hint>
          <mat-error *ngIf="form.get('status')?.hasError('required')">Status is required</mat-error>
        </mat-form-field>

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Monthly Rent</mat-label>
            <input matInput formControlName="rentAmount" type="number" min="0"
                   [placeholder]="selectedUnitRent() ?? 'Unit default'">
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
            <mat-hint>Blank inherits the unit's rent</mat-hint>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Security Deposit</mat-label>
            <input matInput formControlName="securityDepositAmount" type="number" min="0"
                   [placeholder]="selectedUnitDeposit() ?? 'Unit default'">
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
            <mat-hint>Blank inherits the unit's deposit</mat-hint>
          </mat-form-field>
        </div>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting()">Cancel</button>
      <button mat-flat-button color="primary"
              (click)="onSubmit()"
              [disabled]="form.invalid || isSubmitting()">
        <mat-spinner *ngIf="isSubmitting()" diameter="20" class="inline-block mr-2"></mat-spinner>
        {{ isEdit() ? 'Update' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `
})
export class TenancyForm implements OnInit {
  private tenancyService = inject(TenancyService);
  private tenantService = inject(TenantService);
  private propertyService = inject(PropertyService);
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  form: FormGroup;
  readonly statuses = TENANCY_STATUSES;
  readonly formatLabel = formatEnumLabel;

  isEdit = signal(false);
  isSubmitting = signal(false);
  isLoadingTenants = signal(false);
  isLoadingUnits = signal(false);
  serverError = signal<string | null>(null);

  tenants = signal<Tenant[]>([]);
  properties = signal<Property[]>([]);
  units = signal<Unit[]>([]);

  /** The unit currently assigned to the tenancy being edited, which stays selectable. */
  private originalUnitId: string | null;

  selectedUnit = computed(() =>
    this.units().find(u => u.id === this.form?.get('unitId')?.value) ?? null);

  selectedUnitRent = computed(() => {
    const unit = this.selectedUnit();
    return unit ? unit.rentAmount.toString() : null;
  });

  selectedUnitDeposit = computed(() => {
    const unit = this.selectedUnit();
    return unit?.depositAmount != null ? unit.depositAmount.toString() : null;
  });

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<TenancyForm>,
    @Inject(MAT_DIALOG_DATA) public data: { tenancy?: Tenancy }
  ) {
    const tenancy = data?.tenancy;
    this.isEdit.set(!!tenancy);
    this.originalUnitId = tenancy?.unitId ?? null;

    this.form = this.fb.group({
      tenantId: [tenancy?.tenantId ?? '', Validators.required],
      propertyId: [tenancy?.propertyId ?? '', Validators.required],
      unitId: [tenancy?.unitId ?? '', Validators.required],
      startDate: [tenancy?.startDate ? new Date(tenancy.startDate) : null, Validators.required],
      endDate: [tenancy?.endDate ? new Date(tenancy.endDate) : null],
      status: [tenancy?.status ?? TenancyStatus.ACTIVE, Validators.required],
      rentAmount: [tenancy?.rentAmount ?? null],
      securityDepositAmount: [tenancy?.securityDepositAmount ?? null]
    });
  }

  ngOnInit(): void {
    this.loadTenants();
    this.loadProperties();

    const propertyId = this.form.get('propertyId')?.value;
    if (propertyId) {
      this.loadUnits(propertyId);
    }
  }

  tenantLabel(tenant: Tenant): string {
    return tenant.tenantType === TenantType.CORPORATE
      ? tenant.companyName ?? 'Unnamed company'
      : `${tenant.firstName ?? ''} ${tenant.lastName ?? ''}`.trim() || 'Unnamed tenant';
  }

  /**
   * Mirrors the server rule: a unit that is occupied or under maintenance cannot take a
   * new tenancy. The unit already held by the tenancy being edited stays selectable.
   */
  isUnitUnavailable(unit: Unit): boolean {
    if (unit.id === this.originalUnitId) {
      return false;
    }
    return unit.occupancyStatus === 'OCCUPIED' || unit.occupancyStatus === 'UNDER_MAINTENANCE';
  }

  statusHint(): string {
    switch (this.form.get('status')?.value) {
      case TenancyStatus.UPCOMING:
        return 'Must start in the future';
      case TenancyStatus.ACTIVE:
        return 'Must have already started; marks the unit occupied';
      case TenancyStatus.PAST:
      case TenancyStatus.EVICTED:
        return 'Requires an end date';
      default:
        return '';
    }
  }

  onPropertyChange(propertyId: string): void {
    this.form.patchValue({ unitId: '' });
    this.units.set([]);
    if (propertyId) {
      this.loadUnits(propertyId);
    }
  }

  openUnitManager(): void {
    const propertyId = this.form.get('propertyId')?.value;
    if (!propertyId) {
      return;
    }

    this.dialogRef.close();
    this.router.navigate(['/properties', propertyId]);
  }

  private loadTenants(): void {
    this.isLoadingTenants.set(true);
    this.tenantService.getAllTenants(0, 200).subscribe({
      next: res => {
        this.tenants.set(res.data?.content ?? []);
        this.isLoadingTenants.set(false);
      },
      error: () => this.isLoadingTenants.set(false)
    });
  }

  private loadProperties(): void {
    this.propertyService.getAllProperties(0, 200).subscribe({
      next: res => this.properties.set(res.data?.content ?? [])
    });
  }

  private loadUnits(propertyId: string): void {
    this.isLoadingUnits.set(true);
    this.unitService.getUnitsByPropertyId(propertyId, 0, 500).subscribe({
      next: res => {
        this.units.set(res.data?.content ?? []);
        this.isLoadingUnits.set(false);
      },
      error: () => this.isLoadingUnits.set(false)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.serverError.set(null);

    const value = this.form.value;
    const request: TenancyRequest = {
      tenantId: value.tenantId,
      unitId: value.unitId,
      startDate: this.formatDate(value.startDate),
      endDate: value.endDate ? this.formatDate(value.endDate) : null,
      status: value.status,
      rentAmount: this.toAmount(value.rentAmount),
      securityDepositAmount: this.toAmount(value.securityDepositAmount)
    };

    const save$ = this.isEdit()
      ? this.tenancyService.updateTenancy(this.data.tenancy!.id, request)
      : this.tenancyService.createTenancy(request);

    save$.subscribe({
      next: res => {
        this.isSubmitting.set(false);
        this.dialogRef.close(res.data);

        if (res.data?.tenantLoginPassword) {
          this.dialog.open(TenantCredentialsDialog, {
            width: '520px',
            data: {
              email: res.data.tenantEmail ?? request.tenantId,
              password: res.data.tenantLoginPassword,
              tenantName: this.tenantLabel(
                this.tenants().find(t => t.id === request.tenantId) ?? {
                  id: request.tenantId,
                  organizationId: '',
                  tenantType: TenantType.INDIVIDUAL,
                  firstName: 'Tenant',
                  lastName: '',
                  email: '',
                  phoneNumber: '',
                  status: '' as string,
                  createdAt: '',
                  updatedAt: ''
                }
              ),
              unitNumber: this.units().find(u => u.id === request.unitId)?.unitNumber ?? ''
            }
          });
        }
      },
      error: err => {
        this.isSubmitting.set(false);
        // Shown inline rather than as a toast: the dialog stays open so the user
        // can correct whatever the server rejected.
        this.serverError.set(resolveApiMessage(err, 'Could not save the tenancy. Please try again.'));
      }
    });
  }

  /** Empty strings from a number input must become null so the server applies unit defaults. */
  private toAmount(value: unknown): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  /** Sends a plain calendar date so the server's LocalDate is not shifted by the timezone. */
  private formatDate(date: Date | string): string {
    if (typeof date === 'string') {
      return date;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
