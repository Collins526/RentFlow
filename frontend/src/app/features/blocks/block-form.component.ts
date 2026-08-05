import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BlockService, BlockRequest, Block } from '../../core/services/block/block.service';

@Component({
  selector: 'app-block-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title class="!font-bold">{{ data?.block ? 'Edit Block' : 'Add New Block' }}</h2>
    <mat-dialog-content class="!pb-6">
      <form [formGroup]="blockForm" class="space-y-4 mt-2">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Block Name</mat-label>
          <input matInput formControlName="name" placeholder="e.g. Building A, West Wing" required>
          <mat-icon matSuffix>apartment</mat-icon>
          <mat-error *ngIf="blockForm.get('name')?.hasError('required')">Required</mat-error>
          <mat-error *ngIf="blockForm.get('name')?.hasError('maxlength')">Max 100 characters</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Number of Floors</mat-label>
          <input matInput type="number" formControlName="numberOfFloors" min="1">
          <mat-icon matSuffix>layers</mat-icon>
          <mat-error *ngIf="blockForm.get('numberOfFloors')?.hasError('min')">Must be at least 1</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="!px-6 !pb-6">
      <button mat-button mat-dialog-close [disabled]="isSaving()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="blockForm.invalid || isSaving()">
        <span *ngIf="!isSaving()">{{ data?.block ? 'Update' : 'Create' }}</span>
        <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
      </button>
    </mat-dialog-actions>
  `
})
export class BlockFormComponent {
  private fb = inject(FormBuilder);
  private blockService = inject(BlockService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<BlockFormComponent>);
  public data = inject<{ propertyId: string; block?: Block }>(MAT_DIALOG_DATA);

  isSaving = signal(false);

  blockForm: FormGroup = this.fb.group({
    name: [this.data?.block?.name || '', [Validators.required, Validators.maxLength(100)]],
    numberOfFloors: [this.data?.block?.numberOfFloors || 1, [Validators.min(1)]],
    description: [this.data?.block?.description || '']
  });

  onSubmit() {
    if (this.blockForm.valid) {
      this.isSaving.set(true);
      const request: BlockRequest = this.blockForm.value;

      const obs$ = this.data?.block
        ? this.blockService.updateBlock(this.data.block.id, request)
        : this.blockService.createBlock(this.data.propertyId, request);

      obs$.subscribe({
        next: (res) => {
          this.snackBar.open(`Block ${this.data?.block ? 'updated' : 'created'} successfully`, 'Close', {
            duration: 3000, panelClass: ['bg-green-600', 'text-white']
          });
          this.dialogRef.close(res.data);
        },
        error: () => {
          this.isSaving.set(false);
          this.snackBar.open('Failed to save block', 'Close', {
            duration: 3000, panelClass: ['bg-red-600', 'text-white']
          });
        }
      });
    }
  }
}
