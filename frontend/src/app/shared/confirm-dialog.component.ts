import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
}

/**
 * Generic confirmation prompt. Closes with `true` when confirmed, `undefined` otherwise.
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <h2 mat-dialog-title class="!font-bold">{{ data.title }}</h2>
    <mat-dialog-content>
      <p class="text-gray-600">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="!px-6 !pb-6">
      <button mat-button mat-dialog-close>{{ data.cancelLabel || 'Cancel' }}</button>
      <button mat-flat-button
              [color]="data.destructive ? 'warn' : 'primary'"
              (click)="dialogRef.close(true)">
        {{ data.confirmLabel || 'Confirm' }}
      </button>
    </mat-dialog-actions>
  `
})
export class ConfirmDialogComponent {
  public dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
  public data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
