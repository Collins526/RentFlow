import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { OrganizationService } from '../../core/services/organization/organization.service';

@Component({
  selector: 'app-organization-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Organization Settings</h1>
        <p class="text-gray-600 mt-2">Manage your company's profile and contact information.</p>
      </div>

      <div *ngIf="isLoadingData()" class="flex justify-center p-12">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoadingData()" class="!rounded-2xl !shadow-sm border border-gray-100">
        <mat-card-content class="p-6">
          <form [formGroup]="orgForm" (ngSubmit)="onSubmit()" class="space-y-6">
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Name -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Organization Name</mat-label>
                <input matInput type="text" formControlName="name" required>
                <mat-icon matSuffix>business</mat-icon>
                <mat-error *ngIf="orgForm.get('name')?.hasError('required')">Name is required</mat-error>
                <mat-error *ngIf="orgForm.get('name')?.hasError('maxlength')">Max 100 characters</mat-error>
              </mat-form-field>

              <!-- Email -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Contact Email</mat-label>
                <input matInput type="email" formControlName="email">
                <mat-icon matSuffix>email</mat-icon>
                <mat-error *ngIf="orgForm.get('email')?.hasError('email')">Invalid email format</mat-error>
              </mat-form-field>

              <!-- Phone -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Phone Number</mat-label>
                <input matInput type="text" formControlName="phone">
                <mat-icon matSuffix>phone</mat-icon>
                <mat-error *ngIf="orgForm.get('phone')?.hasError('maxlength')">Max 20 characters</mat-error>
              </mat-form-field>

              <!-- Logo URL -->
              <mat-form-field appearance="outline" class="w-full">
                <mat-label>Logo URL</mat-label>
                <input matInput type="text" formControlName="logoUrl">
                <mat-icon matSuffix>image</mat-icon>
                <mat-error *ngIf="orgForm.get('logoUrl')?.hasError('maxlength')">URL too long</mat-error>
              </mat-form-field>
            </div>

            <!-- Address -->
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Physical Address</mat-label>
              <textarea matInput formControlName="address" rows="3"></textarea>
              <mat-error *ngIf="orgForm.get('address')?.hasError('maxlength')">Max 255 characters</mat-error>
            </mat-form-field>

            <div class="flex justify-end pt-4 border-t border-gray-100">
              <button mat-flat-button color="primary" type="submit" class="!px-8 !py-6" [disabled]="orgForm.invalid || isSaving()">
                <span *ngIf="!isSaving()" class="text-base">Save Changes</span>
                <mat-spinner *ngIf="isSaving()" diameter="20" color="accent"></mat-spinner>
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class OrganizationSettingsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orgService = inject(OrganizationService);
  private snackBar = inject(MatSnackBar);

  isLoadingData = signal(true);
  isSaving = signal(false);

  orgForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    phone: ['', Validators.maxLength(20)],
    logoUrl: ['', Validators.maxLength(500)],
    address: ['', Validators.maxLength(255)]
  });

  ngOnInit() {
    this.loadOrganization();
  }

  loadOrganization() {
    this.orgService.getMyOrganization().subscribe({
      next: (res) => {
        if (res.data) {
          this.orgForm.patchValue({
            name: res.data.name || '',
            email: res.data.email || '',
            phone: res.data.phone || '',
            logoUrl: res.data.logoUrl || '',
            address: res.data.address || ''
          });
        }
        this.isLoadingData.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load organization settings', 'Close', { duration: 3000 });
        this.isLoadingData.set(false);
      }
    });
  }

  onSubmit() {
    if (this.orgForm.valid) {
      this.isSaving.set(true);
      this.orgService.updateMyOrganization(this.orgForm.value).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.snackBar.open('Organization updated successfully', 'Close', { 
            duration: 3000,
            panelClass: ['bg-green-600', 'text-white']
          });
        },
        error: () => {
          this.isSaving.set(false);
          this.snackBar.open('Failed to update organization', 'Close', { 
            duration: 3000,
            panelClass: ['bg-red-600', 'text-white']
          });
        }
      });
    }
  }
}
