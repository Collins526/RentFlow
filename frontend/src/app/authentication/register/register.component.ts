import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <mat-card class="max-w-xl w-full space-y-8 p-10 !rounded-2xl !shadow-xl">
        <div>
          <h2 class="mt-2 text-center text-3xl font-extrabold text-gray-900">
            Create an Account
          </h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            Register your organization on RentFlow
          </p>
        </div>
        
        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="mt-8 space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>First Name</mat-label>
              <input matInput type="text" formControlName="firstName" required>
              <mat-error *ngIf="registerForm.get('firstName')?.hasError('required')">First name is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Last Name</mat-label>
              <input matInput type="text" formControlName="lastName" required>
              <mat-error *ngIf="registerForm.get('lastName')?.hasError('required')">Last name is required</mat-error>
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Organization Name</mat-label>
            <input matInput type="text" formControlName="organizationName" required>
            <mat-error *ngIf="registerForm.get('organizationName')?.hasError('required')">Organization name is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Email address</mat-label>
            <input matInput type="email" formControlName="email" required>
            <mat-icon matSuffix>email</mat-icon>
            <mat-error *ngIf="registerForm.get('email')?.hasError('required')">Email is required</mat-error>
            <mat-error *ngIf="registerForm.get('email')?.hasError('email')">Please enter a valid email</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Password</mat-label>
            <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
            <button mat-icon-button matSuffix (click)="togglePassword($event)" type="button">
              <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
            </button>
            <mat-hint>Min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char</mat-hint>
            <mat-error *ngIf="registerForm.get('password')?.hasError('required')">Password is required</mat-error>
          </mat-form-field>

          <div class="text-red-500 text-sm text-center" *ngIf="errorMessage()">
            {{ errorMessage() }}
          </div>

          <div class="pt-4">
            <button mat-flat-button color="primary" type="submit" class="w-full !py-6" [disabled]="registerForm.invalid || isLoading()">
              <span *ngIf="!isLoading()" class="text-lg">Register Organization</span>
              <mat-spinner *ngIf="isLoading()" diameter="24" color="accent"></mat-spinner>
            </button>
          </div>
          
          <div class="text-center mt-4">
            <p class="text-sm text-gray-600">
              Already have an account? 
              <a routerLink="/login" class="font-medium text-blue-600 hover:text-blue-500">Sign in</a>
            </p>
          </div>
        </form>
      </mat-card>
    </div>
  `
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  // Note: Password regex matches the backend requirement
  registerForm: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    organizationName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [
      Validators.required, 
      Validators.minLength(8),
      Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$')
    ]]
  });

  hidePassword = signal(true);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  togglePassword(event: Event) {
    event.preventDefault();
    this.hidePassword.set(!this.hidePassword());
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Registration failed. Please try again.');
        }
      });
    }
  }
}
