import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
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
  BulkUnitRequest,
  UNIT_TYPES,
  formatEnumLabel
} from '../../core/services/unit/unit.service';

const MAX_BULK_COUNT = 200;

@Component({
  selector: 'app-bulk-unit-form',
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
    <h2 mat-dialog-title class="!font-bold">Bulk Add Units</h2>
    <mat-dialog-content class="!pb-6">
      <p class="text-sm text-gray-500 mb-4">
        Generate a run of identical units in one go. All units start as vacant.
      </p>

      <form [formGroup]="bulkForm">
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-x-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Prefix</mat-label>
            <input matInput formControlName="prefix" placeholder="A">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Start At</mat-label>
            <input matInput type="number" formControlName="startNumber" min="0" required>
            <mat-error *ngIf="bulkForm.get('startNumber')?.hasError('required')">Required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>How Many</mat-label>
            <input matInput type="number" formControlName="count" min="1" [max]="maxCount" required>
            <mat-error *ngIf="bulkForm.get('count')?.hasError('required')">Required</mat-error>
            <mat-error *ngIf="bulkForm.get('count')?.hasError('min')">At least 1</mat-error>
            <mat-error *ngIf="bulkForm.get('count')?.hasError('max')">Max {{ maxCount }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Zero Padding</mat-label>
            <input matInput type="number" formControlName="numberPadding" min="0" max="10">
          </mat-form-field>
        </div>

        <!-- Live preview so the numbering scheme is obvious before submitting -->
        <div class="rounded-xl bg-gray-50 border border-gray-200 px-4 py-3 mb-5">
          <p class="text-xs uppercase tracking-wide text-gray-500 font-semibold mb-2">Preview</p>
          <div *ngIf="preview().length; else noPreview" class="flex flex-wrap items-center gap-2">
            <span *ngFor="let number of preview()"
                  class="px-2.5 py-1 rounded-lg bg-white border border-gray-200 text-sm font-medium text-gray-800">
              {{ number }}
            </span>
            <span *ngIf="hiddenCount() > 0" class="text-sm text-gray-500">
              + {{ hiddenCount() }} more &hellip; {{ lastNumber() }}
            </span>
          </div>
          <ng-template #noPreview>
            <span class="text-sm text-gray-400">Enter a start number and count to preview.</span>
          </ng-template>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Unit Type</mat-label>
            <mat-select formControlName="type" required>
              <mat-option *ngFor="let type of unitTypes" [value]="type">{{ label(type) }}</mat-option>
            </mat-select>
            <mat-error *ngIf="bulkForm.get('type')?.hasError('required')">Required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Block</mat-label>
            <mat-select formControlName="blockId">
              <mat-option [value]="null">No block (directly under property)</mat-option>
              <mat-option *ngFor="let block of blocks()" [value]="block.id">{{ block.name }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Floor Number</mat-label>
            <input matInput type="number" formControlName="floorNumber">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Monthly Rent</mat-label>
            <input matInput type="number" formControlName="rentAmount" min="0" required>
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
            <mat-error *ngIf="bulkForm.get('rentAmount')?.hasError('required')">Required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Security Deposit</mat-label>
            <input matInput type="number" formControlName="depositAmount" min="0">
            <span matTextPrefix class="text-gray-500 mr-1">KES&nbsp;</span>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Size (sq ft)</mat-label>
            <input matInput type="number" formControlName="sizeSqFt" min="0">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Bedrooms</mat-label>
            <input matInput type="number" formControlName="bedrooms" min="0">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Bathrooms</mat-label>
            <input matInput type="number" formControlName="bathrooms" min="0">
          </mat-form-field>
        </div>

        <mat-checkbox formControlName="furnished">Furnished</mat-checkbox>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="!px-6 !pb-6">
      <button mat-button mat-dialog-close [disabled]="isSaving()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="bulkForm.invalid || isSaving()">
        <span *ngIf="!isSaving()">Create {{ totalCount() }} Units</span>
        <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
      </button>
    </mat-dialog-actions>
  `
})
export class BulkUnitFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private unitService = inject(UnitService);
  private blockService = inject(BlockService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<BulkUnitFormComponent>);
  public data = inject<{ propertyId: string; blockId?: string }>(MAT_DIALOG_DATA);

  isSaving = signal(false);
  blocks = signal<Block[]>([]);

  unitTypes = UNIT_TYPES;
  label = formatEnumLabel;
  maxCount = MAX_BULK_COUNT;

  bulkForm: FormGroup = this.fb.group({
    prefix: ['A'],
    startNumber: [101, [Validators.required, Validators.min(0)]],
    count: [10, [Validators.required, Validators.min(1), Validators.max(MAX_BULK_COUNT)]],
    numberPadding: [0, [Validators.min(0), Validators.max(10)]],
    type: ['BEDSITTER', [Validators.required]],
    blockId: [this.data?.blockId ?? null],
    floorNumber: [null],
    bedrooms: [1, [Validators.min(0)]],
    bathrooms: [1, [Validators.min(0)]],
    sizeSqFt: [null, [Validators.min(0)]],
    rentAmount: [null, [Validators.required, Validators.min(0)]],
    depositAmount: [null, [Validators.min(0)]],
    furnished: [false]
  });

  /** Mirrors the backend's number generation so the preview never lies. */
  private formValue = toSignal(this.bulkForm.valueChanges, { initialValue: this.bulkForm.value });

  private generatedNumbers = computed<string[]>(() => {
    const value = this.formValue();
    const start = Number(value.startNumber);
    const count = Number(value.count);

    if (!Number.isFinite(start) || !Number.isFinite(count) || count < 1 || count > MAX_BULK_COUNT) {
      return [];
    }

    const prefix = (value.prefix ?? '').trim();
    const padding = Number(value.numberPadding) || 0;

    return Array.from({ length: count }, (_, i) =>
      prefix + String(start + i).padStart(padding, '0')
    );
  });

  totalCount = computed(() => this.generatedNumbers().length);
  preview = computed(() => this.generatedNumbers().slice(0, 6));
  hiddenCount = computed(() => Math.max(0, this.generatedNumbers().length - 6));
  lastNumber = computed(() => this.generatedNumbers().at(-1) ?? '');

  ngOnInit() {
    this.blockService.getBlocksByPropertyId(this.data.propertyId, 0, 100).subscribe({
      next: (res) => {
        if (res.success) {
          this.blocks.set(res.data.content);
        }
      }
    });
  }

  onSubmit() {
    if (this.bulkForm.invalid) {
      return;
    }

    this.isSaving.set(true);
    const request: BulkUnitRequest = this.bulkForm.value;

    this.unitService.bulkCreateUnits(this.data.propertyId, request).subscribe({
      next: (res) => {
        this.snackBar.open(`${res.data.length} units created successfully`, 'Close', {
          duration: 3000, panelClass: ['bg-green-600', 'text-white']
        });
        this.dialogRef.close(res.data);
      },
      error: (err) => {
        this.isSaving.set(false);
        this.snackBar.open(err?.error?.message || 'Failed to create units', 'Close', {
          duration: 5000, panelClass: ['bg-red-600', 'text-white']
        });
      }
    });
  }
}
