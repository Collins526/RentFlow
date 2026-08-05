import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PropertyService, PropertyRequest, Property } from '../../core/services/property/property.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-property-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title class="!font-bold">{{ data?.property ? 'Edit Property' : 'Add New Property' }}</h2>
    <mat-dialog-content class="!pb-6">
      <form [formGroup]="propertyForm" class="space-y-4 mt-2">
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Property Name</mat-label>
            <input matInput formControlName="name" required>
            <mat-error *ngIf="propertyForm.get('name')?.hasError('required')">Required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Property Type</mat-label>
            <mat-select formControlName="type" required>
              <mat-option value="RESIDENTIAL">Residential</mat-option>
              <mat-option value="COMMERCIAL">Commercial</mat-option>
              <mat-option value="MIXED">Mixed Use</mat-option>
            </mat-select>
            <mat-error *ngIf="propertyForm.get('type')?.hasError('required')">Required</mat-error>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Address</mat-label>
          <input matInput formControlName="address" required>
          <mat-error *ngIf="propertyForm.get('address')?.hasError('required')">Required</mat-error>
        </mat-form-field>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>City</mat-label>
            <input matInput formControlName="city">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>State</mat-label>
            <input matInput formControlName="state">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Zip Code</mat-label>
            <input matInput formControlName="zipCode">
          </mat-form-field>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Country</mat-label>
            <input matInput formControlName="country">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option value="ACTIVE">Active</mat-option>
              <mat-option value="INACTIVE">Inactive</mat-option>
              <mat-option value="UNDER_CONSTRUCTION">Under Construction</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    
    <mat-dialog-actions align="end" class="!px-6 !pb-6">
      <button mat-button mat-dialog-close [disabled]="isSaving()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="propertyForm.invalid || isSaving()">
        <span *ngIf="!isSaving()">{{ data?.property ? 'Update' : 'Create' }}</span>
        <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
      </button>
    </mat-dialog-actions>
  `
})
export class PropertyFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<PropertyFormComponent>);
  public data = inject<{property?: Property}>(MAT_DIALOG_DATA, { optional: true });

  isSaving = signal(false);

  propertyForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    type: ['', Validators.required],
    address: ['', [Validators.required, Validators.maxLength(255)]],
    city: ['', Validators.maxLength(100)],
    state: ['', Validators.maxLength(100)],
    zipCode: ['', Validators.maxLength(20)],
    country: ['', Validators.maxLength(100)],
    description: [''],
    status: ['ACTIVE']
  });

  ngOnInit() {
    if (this.data?.property) {
      this.propertyForm.patchValue(this.data.property);
    }
  }

  onSubmit() {
    if (this.propertyForm.valid) {
      this.isSaving.set(true);
      const request: PropertyRequest = this.propertyForm.value;

      const obs$ = this.data?.property 
        ? this.propertyService.updateProperty(this.data.property.id, request)
        : this.propertyService.createProperty(request);

      obs$.subscribe({
        next: (res) => {
          this.snackBar.open(`Property ${this.data?.property ? 'updated' : 'created'} successfully`, 'Close', { duration: 3000, panelClass: ['bg-green-600', 'text-white'] });
          this.dialogRef.close(res.data);
        },
        error: () => {
          this.isSaving.set(false);
          this.snackBar.open('Failed to save property', 'Close', { duration: 3000, panelClass: ['bg-red-600', 'text-white'] });
        }
      });
    }
  }
}
