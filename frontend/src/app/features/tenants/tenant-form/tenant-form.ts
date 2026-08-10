import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TenantService } from '../../../core/services/tenant/tenant.service';
import { resolveApiMessage } from '../../../core/services/toast.service';
import { Tenant, TenantType } from '../../../core/models/tenant.model';

@Component({
  selector: 'app-tenant-form',
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
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit() ? 'Edit Tenant' : 'Add Tenant' }}</h2>
    
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 mt-4">

        <div *ngIf="serverError()"
             class="flex items-start gap-2 rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-800">
          <mat-icon class="!text-lg !h-5 !w-5">error_outline</mat-icon>
          <span>{{ serverError() }}</span>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Tenant Type</mat-label>
          <mat-select formControlName="tenantType">
            <mat-option value="INDIVIDUAL">Individual</mat-option>
            <mat-option value="CORPORATE">Corporate</mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('tenantType')?.hasError('required')">Type is required</mat-error>
        </mat-form-field>

        <div *ngIf="form.get('tenantType')?.value === 'INDIVIDUAL'" class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>First Name</mat-label>
            <input matInput formControlName="firstName" placeholder="John">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Last Name</mat-label>
            <input matInput formControlName="lastName" placeholder="Doe">
          </mat-form-field>
        </div>

        <mat-form-field *ngIf="form.get('tenantType')?.value === 'CORPORATE'" appearance="outline" class="w-full">
          <mat-label>Company Name</mat-label>
          <input matInput formControlName="companyName" placeholder="Acme Corp">
        </mat-form-field>

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" type="email" placeholder="john@example.com">
            <mat-error *ngIf="form.get('email')?.hasError('required')">Email is required</mat-error>
            <mat-error *ngIf="form.get('email')?.hasError('email')">Invalid email format</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Phone Number</mat-label>
            <input matInput formControlName="phoneNumber" placeholder="+1234567890">
            <mat-error *ngIf="form.get('phoneNumber')?.hasError('required')">Phone is required</mat-error>
          </mat-form-field>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>ID Type</mat-label>
            <mat-select formControlName="identificationType">
              <mat-option value="NATIONAL_ID">National ID</mat-option>
              <mat-option value="PASSPORT">Passport</mat-option>
              <mat-option value="COMPANY_REGISTRATION">Company Registration</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>ID Number</mat-label>
            <input matInput formControlName="identificationNumber">
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full" *ngIf="isEdit()">
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ACTIVE">Active</mat-option>
            <mat-option value="INACTIVE">Inactive</mat-option>
          </mat-select>
        </mat-form-field>

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
export class TenantForm implements OnInit {
  form: FormGroup;
  isEdit = signal(false);
  isSubmitting = signal(false);
  serverError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private tenantService: TenantService,
    public dialogRef: MatDialogRef<TenantForm>,
    @Inject(MAT_DIALOG_DATA) public data: { tenant?: Tenant }
  ) {
    this.isEdit.set(!!data?.tenant);
    
    this.form = this.fb.group({
      tenantType: [data?.tenant?.tenantType || 'INDIVIDUAL', Validators.required],
      firstName: [data?.tenant?.firstName || ''],
      lastName: [data?.tenant?.lastName || ''],
      companyName: [data?.tenant?.companyName || ''],
      email: [data?.tenant?.email || '', [Validators.required, Validators.email]],
      phoneNumber: [data?.tenant?.phoneNumber || '', Validators.required],
      identificationType: [data?.tenant?.identificationType || ''],
      identificationNumber: [data?.tenant?.identificationNumber || ''],
      status: [data?.tenant?.status || 'ACTIVE']
    });

    // Handle required fields based on tenant type
    this.form.get('tenantType')?.valueChanges.subscribe(type => {
      const isIndividual = type === 'INDIVIDUAL';
      const firstNameCtrl = this.form.get('firstName');
      const lastNameCtrl = this.form.get('lastName');
      const companyCtrl = this.form.get('companyName');

      if (isIndividual) {
        firstNameCtrl?.setValidators(Validators.required);
        lastNameCtrl?.setValidators(Validators.required);
        companyCtrl?.clearValidators();
      } else {
        firstNameCtrl?.clearValidators();
        lastNameCtrl?.clearValidators();
        companyCtrl?.setValidators(Validators.required);
      }
      
      firstNameCtrl?.updateValueAndValidity();
      lastNameCtrl?.updateValueAndValidity();
      companyCtrl?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    // Trigger initial validation setup
    this.form.get('tenantType')?.updateValueAndValidity();
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.serverError.set(null);
    const request = this.form.value;

    const requestObservable = this.isEdit()
      ? this.tenantService.updateTenant(this.data.tenant!.id, request)
      : this.tenantService.createTenant(request);

    requestObservable.subscribe({
      next: (res) => {
        this.isSubmitting.set(false);
        this.dialogRef.close(res.data);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        // Shown inline rather than as a toast: the dialog stays open so the user
        // can correct the field the server rejected.
        this.serverError.set(resolveApiMessage(err, 'Could not save the tenant.'));
      }
    });
  }
}
