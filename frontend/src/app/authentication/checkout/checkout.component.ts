import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';

interface CheckoutPlan {
  code: 'STARTER' | 'GROWTH';
  name: string;
  price: number;
  units: string;
}

const CHECKOUT_PLANS: Record<string, CheckoutPlan> = {
  STARTER: { code: 'STARTER', name: 'Starter', price: 2500, units: 'Up to 25 units' },
  GROWTH: { code: 'GROWTH', name: 'Growth', price: 6500, units: 'Up to 150 units' }
};

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  template: `
    <main class="min-h-screen bg-slate-100 px-4 py-8 text-slate-900 sm:py-12">
      <div class="mx-auto max-w-5xl">
        <a routerLink="/" class="inline-flex items-center gap-2 text-lg font-semibold text-slate-900">
          <img src="/rentflow-mark.svg" alt="" class="h-8 w-8 rounded-lg" />
          RentFlow
        </a>

        <div *ngIf="!plan" class="mt-8 rounded-xl border border-rose-200 bg-white p-6">
          <h1 class="text-xl font-semibold">Plan not found</h1>
          <p class="mt-2 text-sm text-slate-600">Choose Starter or Growth from the pricing section.</p>
          <a routerLink="/" fragment="pricing" class="mt-4 inline-block text-sm font-semibold text-indigo-700">Back to pricing</a>
        </div>

        <div *ngIf="plan" class="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px]">
          <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-8">
            <a routerLink="/" fragment="pricing" class="mb-6 inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-900">
              <mat-icon class="!h-4 !w-4 !text-base">arrow_back</mat-icon>
              Back to pricing
            </a>

            <div class="border-b border-slate-200 pb-6">
              <p class="text-xs font-semibold uppercase tracking-wider text-indigo-700">30-day free trial</p>
              <h1 class="mt-2 text-2xl font-bold">Start your {{ plan.name }} trial</h1>
              <p class="mt-2 text-sm leading-relaxed text-slate-600">
                No payment due today. After 30 days, renew manually with an M-Pesa STK prompt.
                RentFlow will not charge you automatically.
              </p>
            </div>

            <form [formGroup]="checkoutForm" (ngSubmit)="submit()" class="mt-6 space-y-6">
              <section aria-labelledby="contact-heading">
                <h2 id="contact-heading" class="text-base font-semibold">Contact information</h2>
                <div class="mt-4 grid gap-4 sm:grid-cols-2">
                  <mat-form-field appearance="outline" class="w-full">
                    <mat-label>First name</mat-label>
                    <input matInput formControlName="firstName" autocomplete="given-name" />
                    <mat-error>First name is required.</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="w-full">
                    <mat-label>Last name</mat-label>
                    <input matInput formControlName="lastName" autocomplete="family-name" />
                    <mat-error>Last name is required.</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="w-full sm:col-span-2">
                    <mat-label>Work email</mat-label>
                    <input matInput type="email" formControlName="email" autocomplete="email" />
                    <mat-error *ngIf="checkoutForm.controls.email.hasError('required')">Email is required.</mat-error>
                    <mat-error *ngIf="checkoutForm.controls.email.hasError('email')">Enter a valid email address.</mat-error>
                    <mat-error *ngIf="checkoutForm.controls.email.hasError('maxlength')">Email must be 150 characters or fewer.</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="w-full sm:col-span-2">
                    <mat-label>Organization name</mat-label>
                    <input matInput formControlName="organizationName" autocomplete="organization" />
                    <mat-error>Organization name is required.</mat-error>
                  </mat-form-field>
                </div>
              </section>

              <section aria-labelledby="payment-heading" class="border-t border-slate-200 pt-6">
                <h2 id="payment-heading" class="text-base font-semibold">Payment method</h2>
                <div class="mt-4 rounded-lg border border-slate-200 p-4">
                  <div class="flex items-center gap-2 font-medium">
                    <mat-icon class="text-emerald-700">phone_android</mat-icon>
                    M-Pesa STK
                  </div>
                  <mat-form-field appearance="outline" class="mt-3 w-full">
                    <mat-label>M-Pesa phone number</mat-label>
                    <input matInput type="tel" formControlName="phoneNumber" autocomplete="tel" placeholder="0712 345 678" />
                    <mat-hint>Used for manual renewal after your trial.</mat-hint>
                    <mat-error *ngIf="checkoutForm.controls.phoneNumber.hasError('required')">Phone number is required.</mat-error>
                    <mat-error *ngIf="checkoutForm.controls.phoneNumber.hasError('pattern')">Enter a valid Kenyan mobile number.</mat-error>
                  </mat-form-field>
                  <p class="mt-2 text-xs leading-relaxed text-slate-500">
                    We will request payment only when you choose to renew. Your phone will not be charged during signup.
                  </p>
                </div>
              </section>

              <section aria-labelledby="account-heading" class="border-t border-slate-200 pt-6">
                <h2 id="account-heading" class="text-base font-semibold">Secure your account</h2>
                <mat-form-field appearance="outline" class="mt-4 w-full">
                  <mat-label>Password</mat-label>
                  <input matInput type="password" formControlName="password" autocomplete="new-password" />
                  <mat-hint>At least 8 characters with uppercase, lowercase, number, and symbol.</mat-hint>
                  <mat-error *ngIf="checkoutForm.controls.password.hasError('required')">Password is required.</mat-error>
                  <mat-error *ngIf="checkoutForm.controls.password.hasError('minlength') || checkoutForm.controls.password.hasError('pattern')">Use at least 8 characters with uppercase, lowercase, number, and symbol.</mat-error>
                </mat-form-field>
              </section>

              <p *ngIf="errorMessage()" role="alert" class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">
                {{ errorMessage() }}
              </p>

              <button mat-flat-button color="primary" type="submit" class="!h-12 !w-full !rounded-lg !text-base"
                      [disabled]="submitting()">
                <mat-spinner *ngIf="submitting()" diameter="22"></mat-spinner>
                <span *ngIf="!submitting()">Start 30-day free trial</span>
              </button>
              <p class="text-center text-xs leading-relaxed text-slate-500">
                By starting your trial, you agree to the RentFlow terms. No payment is taken today.
              </p>
            </form>
          </section>

          <aside class="h-fit rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
            <h2 class="text-base font-semibold">Order summary</h2>
            <div class="mt-5 flex items-start justify-between gap-4">
              <div>
                <p class="font-medium">RentFlow {{ plan.name }}</p>
                <p class="mt-1 text-sm text-slate-500">{{ plan.units }}</p>
              </div>
              <span class="text-sm font-semibold">KES {{ plan.price | number }}</span>
            </div>
            <div class="mt-5 border-t border-slate-200 pt-4">
              <div class="flex justify-between text-sm">
                <span class="text-slate-600">Due today</span>
                <span class="font-semibold">KES 0</span>
              </div>
              <div class="mt-3 flex justify-between text-sm">
                <span class="text-slate-600">After 30 days</span>
                <span class="font-semibold">KES {{ plan.price | number }} / month</span>
              </div>
              <p class="mt-3 text-xs leading-relaxed text-slate-500">
                Manual renewal by M-Pesa STK. No automatic billing.
              </p>
            </div>
          </aside>
        </div>
      </div>
    </main>
  `
})
export class CheckoutComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  readonly plan: CheckoutPlan | null;
  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly checkoutForm = this.formBuilder.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    organizationName: ['', [Validators.required, Validators.maxLength(100)]],
    phoneNumber: ['', [Validators.required, Validators.maxLength(20), Validators.pattern('^(?:\\+?254|0)?[17]\\d{8}$')]],
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$')
    ]]
  });

  constructor(route: ActivatedRoute) {
    const planCode = route.snapshot.paramMap.get('planCode')?.toUpperCase() ?? '';
    this.plan = CHECKOUT_PLANS[planCode] ?? null;
  }

  submit(): void {
    if (!this.plan || this.checkoutForm.invalid || this.submitting()) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');
    this.authService.register({
      ...this.checkoutForm.getRawValue(),
      planCode: this.plan.code
    }).subscribe({
      error: error => {
        this.submitting.set(false);
        this.errorMessage.set(error.error?.message || 'Trial signup failed. Please try again.');
      }
    });
  }
}