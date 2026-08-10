import { Component, Inject, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TenancyService } from '../../../core/services/tenancy/tenancy.service';
import { resolveApiMessage } from '../../../core/services/toast.service';
import { Tenancy, TenancyStatus } from '../../../core/models/tenancy.model';

/**
 * Closes out a tenancy. Separate from the edit form because ending a tenancy is a
 * lifecycle action — it releases the unit — rather than a correction to the record.
 */
@Component({
  selector: 'app-tenancy-end-dialog',
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
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>End Tenancy</h2>

    <mat-dialog-content>
      <p class="text-gray-600 mb-4">
        Closing <span class="font-medium text-gray-900">{{ data.tenancy.tenantName }}</span>'s tenancy of unit
        <span class="font-medium text-gray-900">{{ data.tenancy.unitNumber }}</span>.
        The unit will be released back to vacant.
      </p>

      <div *ngIf="serverError()"
           class="flex items-start gap-2 rounded-lg bg-red-50 border border-red-200 px-3 py-2 mb-4 text-sm text-red-800">
        <mat-icon class="!text-lg !h-5 !w-5">error_outline</mat-icon>
        <span>{{ serverError() }}</span>
      </div>

      <form [formGroup]="form" class="flex flex-col gap-4">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>End Date</mat-label>
          <input matInput [matDatepicker]="picker" formControlName="endDate">
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
          <mat-error *ngIf="form.get('endDate')?.hasError('required')">End date is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Reason</mat-label>
          <mat-select formControlName="status">
            <mat-option value="PAST">Tenancy ended normally</mat-option>
            <mat-option value="EVICTED">Tenant evicted</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting()">Cancel</button>
      <button mat-flat-button color="warn" (click)="onConfirm()" [disabled]="form.invalid || isSubmitting()">
        <mat-spinner *ngIf="isSubmitting()" diameter="20" class="inline-block mr-2"></mat-spinner>
        End Tenancy
      </button>
    </mat-dialog-actions>
  `
})
export class TenancyEndDialog {
  private tenancyService = inject(TenancyService);

  form: FormGroup;
  isSubmitting = signal(false);
  serverError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<TenancyEndDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { tenancy: Tenancy }
  ) {
    this.form = this.fb.group({
      endDate: [new Date(), Validators.required],
      status: [TenancyStatus.PAST, Validators.required]
    });
  }

  onConfirm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.serverError.set(null);

    this.tenancyService.endTenancy(this.data.tenancy.id, {
      endDate: this.formatDate(this.form.value.endDate),
      status: this.form.value.status
    }).subscribe({
      next: res => {
        this.isSubmitting.set(false);
        this.dialogRef.close(res.data);
      },
      error: err => {
        this.isSubmitting.set(false);
        this.serverError.set(resolveApiMessage(err, 'Could not end the tenancy. Please try again.'));
      }
    });
  }

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
