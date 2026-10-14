import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { InvoiceService, OwnerInvoice } from '../../core/services/invoice/invoice.service';
import { resolveApiMessage } from '../../core/services/toast.service';

@Component({
  selector: 'app-invoice-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>Edit invoice</h2>
    <mat-dialog-content>
      <p class="mb-5 text-sm text-slate-600">
        Update the billing period, amount, deadline, or tenant message for
        {{ data.tenantName || 'this tenant' }} · {{ data.unitNumber || 'Unit' }}.
      </p>

      <form [formGroup]="form" class="space-y-4" (ngSubmit)="save()">
        <div class="grid gap-4 sm:grid-cols-2">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Period start</mat-label>
            <input matInput type="date" formControlName="periodStart">
            <mat-error>Period start is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Period end</mat-label>
            <input matInput type="date" formControlName="periodEnd">
            <mat-error>Period end is required</mat-error>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Amount (KES)</mat-label>
          <input matInput type="number" min="0.01" step="0.01" formControlName="amount">
          <mat-error>Enter a valid amount</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Payment deadline</mat-label>
          <input matInput type="date" formControlName="dueDate">
          <mat-error>Payment deadline is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Message to tenant (optional)</mat-label>
          <textarea matInput rows="3" formControlName="notes"></textarea>
        </mat-form-field>

        <div *ngIf="error()" class="mb-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
          {{ error() }}
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close()" [disabled]="saving()">Cancel</button>
      <button mat-flat-button color="primary" type="button" (click)="save()" [disabled]="form.invalid || saving()">
        <mat-icon>save</mat-icon> {{ saving() ? 'Saving…' : 'Save changes' }}
      </button>
    </mat-dialog-actions>
  `
})
export class InvoiceEditDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly invoiceService = inject(InvoiceService);
  readonly data = inject<OwnerInvoice>(MAT_DIALOG_DATA);
  readonly dialogRef = inject<MatDialogRef<InvoiceEditDialogComponent, OwnerInvoice>>(MatDialogRef);

  saving = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    periodStart: [this.data.periodStart, Validators.required],
    periodEnd: [this.data.periodEnd, Validators.required],
    amount: [this.data.amount, [Validators.required, Validators.min(0.01)]],
    dueDate: [this.data.dueDate, Validators.required],
    notes: [this.data.notes ?? '']
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (!value.periodStart || !value.periodEnd || !value.dueDate || value.amount == null) {
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.invoiceService.updateInvoice(this.data.id, {
      tenancyId: this.data.tenancyId ?? null,
      tenantId: this.data.tenantId,
      unitId: this.data.unitId,
      periodStart: value.periodStart,
      periodEnd: value.periodEnd,
      dueDate: value.dueDate,
      amount: value.amount,
      notes: value.notes
    }).subscribe({
      next: response => {
        this.saving.set(false);
        if (!response.success || !response.data) {
          this.error.set(response.message || 'Could not update the invoice.');
          return;
        }
        this.dialogRef.close(response.data);
      },
      error: err => {
        this.saving.set(false);
        this.error.set(resolveApiMessage(err, 'Could not update the invoice.'));
      }
    });
  }
}
