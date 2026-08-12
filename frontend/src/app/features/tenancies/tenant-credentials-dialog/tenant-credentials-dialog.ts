import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-tenant-credentials-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>Tenant Login Credentials</h2>

    <mat-dialog-content class="space-y-4">
      <p class="text-gray-700">
        The tenant account for <strong>{{ data.tenantName }}</strong> has been created or updated for unit
        <strong>{{ data.unitNumber }}</strong>.
      </p>

      <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
        <div class="text-sm text-slate-600 mb-3">Share these credentials securely with the tenant:</div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Email</mat-label>
          <input matInput [value]="data.email" readonly>
          <button mat-icon-button matSuffix aria-label="Copy email" (click)="copyText(data.email)">
            <mat-icon>content_copy</mat-icon>
          </button>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Password</mat-label>
          <input matInput [value]="data.password" readonly>
          <button mat-icon-button matSuffix aria-label="Copy password" (click)="copyText(data.password)">
            <mat-icon>content_copy</mat-icon>
          </button>
        </mat-form-field>
      </div>

      <p class="text-sm text-slate-600">
        The tenant can use these credentials to log in, raise issues, and pay for the assigned unit.
        You may want to prompt them to change the password after first login.
      </p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-flat-button color="primary" (click)="dialogRef.close()">
        OK
      </button>
    </mat-dialog-actions>
  `
})
export class TenantCredentialsDialog {
  constructor(
    public dialogRef: MatDialogRef<TenantCredentialsDialog>,
    @Inject(MAT_DIALOG_DATA)
    public data: { email: string; password: string; tenantName: string; unitNumber: string }
  ) {}

  copyText(value: string): void {
    if (!value) {
      return;
    }

    navigator.clipboard.writeText(value).catch(() => {
      // Clipboard may be unavailable in some browsers; silently ignore.
    });
  }
}
