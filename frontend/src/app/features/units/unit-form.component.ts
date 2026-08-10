import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BlockService, Block } from '../../core/services/block/block.service';
import {
  UnitService,
  Unit,
  UnitRequest,
  UNIT_TYPES,
  formatEnumLabel
} from '../../core/services/unit/unit.service';

@Component({
  selector: 'app-unit-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title class="!font-bold">{{ data?.unit ? 'Edit Unit' : 'Add New Unit' }}</h2>
    <mat-dialog-content class="!pb-6">
      <form [formGroup]="unitForm" class="mt-2">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Unit Number</mat-label>
            <input matInput formControlName="unitNumber" placeholder="e.g. A101" required>
            <mat-icon matSuffix>tag</mat-icon>
            <mat-error *ngIf="unitForm.get('unitNumber')?.hasError('required')">Required</mat-error>
            <mat-error *ngIf="unitForm.get('unitNumber')?.hasError('maxlength')">Max 50 characters</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Unit Type</mat-label>
            <mat-select formControlName="type" required>
              <mat-option *ngFor="let type of unitTypes" [value]="type">{{ label(type) }}</mat-option>
            </mat-select>
            <mat-error *ngIf="unitForm.get('type')?.hasError('required')">Required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Block</mat-label>
            <mat-select formControlName="blockId">
              <mat-option [value]="null">No block (directly under property)</mat-option>
              <mat-option *ngFor="let block of blocks()" [value]="block.id">{{ block.name }}</mat-option>
            </mat-select>
            <mat-hint *ngIf="!blocks().length">This property has no blocks yet</mat-hint>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Floor Number</mat-label>
            <input matInput type="number" formControlName="floorNumber">
            <mat-icon matSuffix>layers</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Bedrooms</mat-label>
            <input matInput type="number" formControlName="bedrooms" min="0">
            <mat-error *ngIf="unitForm.get('bedrooms')?.hasError('min')">Cannot be negative</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Bathrooms</mat-label>
            <input matInput type="number" formControlName="bathrooms" min="0">
            <mat-error *ngIf="unitForm.get('bathrooms')?.hasError('min')">Cannot be negative</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Monthly Rent</mat-label>
            <input matInput type="number" formControlName="rentAmount" min="0" required>
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
            <mat-error *ngIf="unitForm.get('rentAmount')?.hasError('required')">Required</mat-error>
            <mat-error *ngIf="unitForm.get('rentAmount')?.hasError('min')">Cannot be negative</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Security Deposit</mat-label>
            <input matInput type="number" formControlName="depositAmount" min="0">
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
            <mat-error *ngIf="unitForm.get('depositAmount')?.hasError('min')">Cannot be negative</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Size (sq ft)</mat-label>
            <input matInput type="number" formControlName="sizeSqFt" min="0">
            <mat-icon matSuffix>straighten</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Water Meter No.</mat-label>
            <input matInput formControlName="waterMeterNumber">
            <mat-icon matSuffix>water_drop</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Electricity Meter No.</mat-label>
            <input matInput formControlName="electricityMeterNumber">
            <mat-icon matSuffix>bolt</mat-icon>
          </mat-form-field>
        </div>

        <mat-checkbox formControlName="furnished" class="!mb-4">Furnished</mat-checkbox>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="2"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="!px-6 !pb-6">
      <button mat-button mat-dialog-close [disabled]="isSaving()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="unitForm.invalid || isSaving()">
        <span *ngIf="!isSaving()">{{ data?.unit ? 'Update' : 'Create' }}</span>
        <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
      </button>
    </mat-dialog-actions>
  `
})
export class UnitFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private unitService = inject(UnitService);
  private blockService = inject(BlockService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<UnitFormComponent>);
  public data = inject<{ propertyId: string; unit?: Unit; blockId?: string }>(MAT_DIALOG_DATA);

  isSaving = signal(false);
  blocks = signal<Block[]>([]);

  unitTypes = UNIT_TYPES;
  label = formatEnumLabel;

  unitForm: FormGroup = this.fb.group({
    unitNumber: [this.data?.unit?.unitNumber ?? '', [Validators.required, Validators.maxLength(50)]],
    type: [this.data?.unit?.type ?? 'ONE_BEDROOM', [Validators.required]],
    blockId: [this.data?.unit?.blockId ?? this.data?.blockId ?? null],
    floorNumber: [this.data?.unit?.floorNumber ?? null],
    bedrooms: [this.data?.unit?.bedrooms ?? 1, [Validators.min(0)]],
    bathrooms: [this.data?.unit?.bathrooms ?? 1, [Validators.min(0)]],
    sizeSqFt: [this.data?.unit?.sizeSqFt ?? null, [Validators.min(0)]],
    rentAmount: [this.data?.unit?.rentAmount ?? null, [Validators.required, Validators.min(0)]],
    depositAmount: [this.data?.unit?.depositAmount ?? null, [Validators.min(0)]],
    furnished: [this.data?.unit?.furnished ?? false],
    waterMeterNumber: [this.data?.unit?.waterMeterNumber ?? ''],
    electricityMeterNumber: [this.data?.unit?.electricityMeterNumber ?? ''],
    description: [this.data?.unit?.description ?? '']
  });

  ngOnInit() {
    // Load every block of the property so the user can place the unit.
    this.blockService.getBlocksByPropertyId(this.data.propertyId, 0, 100).subscribe({
      next: (res) => {
        if (res.success) {
          this.blocks.set(res.data.content);
        }
      }
    });
  }

  onSubmit() {
    if (this.unitForm.invalid) {
      return;
    }

    this.isSaving.set(true);
    const request: UnitRequest = this.unitForm.value;

    const obs$ = this.data?.unit
      ? this.unitService.updateUnit(this.data.unit.id, request)
      : this.unitService.createUnit(this.data.propertyId, request);

    obs$.subscribe({
      next: (res) => {
        this.snackBar.open(`Unit ${this.data?.unit ? 'updated' : 'created'} successfully`, 'Close', {
          duration: 3000, panelClass: ['bg-green-600', 'text-white']
        });
        this.dialogRef.close(res.data);
      },
      error: (err) => {
        this.isSaving.set(false);
        this.snackBar.open(err?.error?.message || 'Failed to save unit', 'Close', {
          duration: 4000, panelClass: ['bg-red-600', 'text-white']
        });
      }
    });
  }
}
