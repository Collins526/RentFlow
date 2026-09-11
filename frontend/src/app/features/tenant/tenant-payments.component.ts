import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CreateTenantPaymentRequest, TenantPayment, TenantPortalService } from '../../core/services/tenant/tenant-portal.service';

@Component({
  selector: 'app-tenant-payments',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <div class="space-y-6 p-6">
      <div class="flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-medium uppercase tracking-wide text-indigo-600">Tenant portal</p>
          <h1 class="mt-1 text-2xl font-semibold text-slate-900">Payments</h1>
        </div>
        <a mat-stroked-button routerLink="/tenant">
          <mat-icon>arrow_back</mat-icon>
          Back to portal
        </a>
      </div>

      <div *ngIf="loading()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">
        Loading payments…
      </div>

      <div *ngIf="error()" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
        {{ error() }}
      </div>

      <div *ngIf="!loading() && !error()" class="space-y-4">
        <div class="rounded-2xl border border-indigo-200 bg-indigo-50 p-5 shadow-sm">
          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold uppercase tracking-wide text-indigo-700">Quick action</p>
              <h2 class="mt-1 text-xl font-semibold text-slate-900">Make a payment</h2>
            </div>
            <div class="flex flex-wrap items-center gap-3">
              <button mat-flat-button color="primary" type="button" (click)="selectPaymentType('rent')">
                Pay rent
              </button>
              <button mat-stroked-button color="primary" type="button" (click)="selectPaymentType('security-deposit')">
                Pay security deposit
              </button>
            </div>
          </div>

          <div *ngIf="paymentType()" class="mt-3 rounded-xl border border-indigo-200 bg-white px-3 py-2 text-sm text-slate-700">
            Selected payment: <span class="font-semibold text-indigo-700">{{ paymentType() === 'rent' ? 'Rent' : 'Security deposit' }}</span>
          </div>
        </div>

        <div *ngIf="paymentType()" class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div class="mb-4">
            <h3 class="text-lg font-semibold text-slate-900">{{ paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment' }}</h3>
            <p class="mt-1 text-sm text-slate-500">
              Submit a payment for your {{ paymentType() === 'rent' ? 'monthly rent' : 'security deposit' }}.
            </p>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Amount (KES)</mat-label>
              <input matInput type="number" min="0" step="1" [(ngModel)]="amount" name="amount" placeholder="5000" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Payment method</mat-label>
              <mat-select [(ngModel)]="method" name="method">
                <mat-option value="MPESA">MPESA</mat-option>
                <mat-option value="CASH">Cash</mat-option>
                <mat-option value="CARD">Card</mat-option>
                <mat-option value="BANK_TRANSFER">Bank transfer</mat-option>
                <mat-option value="MOBILE_MONEY">Mobile money</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full md:col-span-2">
              <mat-label>Phone number</mat-label>
              <input matInput type="tel" [(ngModel)]="phoneNumber" name="phoneNumber" placeholder="254712345678" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-full md:col-span-2">
              <mat-label>Reference</mat-label>
              <input matInput type="text" [(ngModel)]="reference" name="reference" [placeholder]="paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment'" />
            </mat-form-field>
          </div>

          <div
            *ngIf="submitMessage()"
            class="mt-4 rounded-xl border px-3 py-3 text-sm"
            [ngClass]="submitState() === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-amber-200 bg-amber-50 text-amber-700'"
          >
            <div class="flex items-center justify-between gap-3">
              <span>{{ submitMessage() }}</span>
              <span
                *ngIf="method === 'MPESA' && isAwaitingMpesaConfirmation()"
                class="rounded-full bg-amber-100 px-2 py-1 text-[11px] font-semibold uppercase tracking-wide text-amber-800"
              >
                Awaiting confirmation
              </span>
            </div>
          </div>

          <div class="mt-5 flex justify-end">
            <button mat-flat-button color="primary" type="button" [disabled]="submitting()" (click)="submitPayment()">
              {{ submitting() ? 'Submitting…' : 'Submit payment' }}
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-sm font-semibold uppercase tracking-wide text-indigo-700">Payment history</p>
            <h2 class="mt-1 text-xl font-semibold text-slate-900">Payments made</h2>
          </div>
          <button
            mat-stroked-button
            type="button"
            [attr.aria-label]="paymentsVisible() ? 'Hide payments' : 'Show payments'"
            (click)="togglePaymentsVisibility()"
          >
            <mat-icon>{{ paymentsVisible() ? 'visibility_off' : 'visibility' }}</mat-icon>
            {{ paymentsVisible() ? 'Hide payments' : 'Show payments' }}
          </button>
        </div>

        <div *ngIf="!paymentsVisible()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">
          Payment details are hidden. Click “Show payments” to view your history.
        </div>

        <div *ngIf="paymentsVisible() && items().length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-slate-500">
          No payments have been recorded for your account yet.
        </div>

        <div *ngFor="let item of paymentsVisible() ? items() : []" class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-sm text-slate-500">Reference {{ item.reference || 'N/A' }}</p>
              <h2 class="text-lg font-semibold text-slate-900">{{ item.method || 'Payment' }}</h2>
            </div>
            <div class="text-right">
              <p class="text-xl font-bold text-slate-900">{{ item.amount | currency:'KES ':'symbol':'1.0-2' }}</p>
              <span
                class="rounded-full px-2 py-1 text-xs font-medium"
                [ngClass]="item.status === 'COMPLETED' ? 'bg-emerald-50 text-emerald-700' : item.status === 'FAILED' ? 'bg-rose-50 text-rose-700' : 'bg-amber-50 text-amber-700'"
              >
                {{ item.status || 'PENDING' }}
              </span>
            </div>
          </div>
          <div class="mt-3 text-sm text-slate-500">
            Paid on {{ (item.paymentDate || item.id) | date:'mediumDate' }}
          </div>
        </div>
      </div>
    </div>
  `,
  providers: [CurrencyPipe, DatePipe]
})
export class TenantPaymentsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly service = inject(TenantPortalService);

  items = signal<TenantPayment[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  paymentType = signal<'rent' | 'security-deposit' | null>(null);
  submitting = signal(false);
  submitMessage = signal<string | null>(null);
  submitState = signal<'success' | 'error' | null>(null);
  isAwaitingMpesaConfirmation = signal(false);
  paymentsVisible = signal(true);

  private mpesaPollTimer: any = null;

  amount = '';
  method = 'MPESA';
  phoneNumber = '';
  reference = '';

  togglePaymentsVisibility(): void {
    this.paymentsVisible.update(visible => !visible);
  }

  selectPaymentType(type: 'rent' | 'security-deposit'): void {
    this.paymentType.set(type);
    this.submitMessage.set(null);
    this.submitState.set(null);
    this.isAwaitingMpesaConfirmation.set(false);
    this.reference = type === 'rent' ? 'Rent payment' : 'Security deposit payment';
    this.clearMpesaPolling();
  }

  private clearMpesaPolling(): void {
    if (this.mpesaPollTimer) {
      window.clearInterval(this.mpesaPollTimer);
      this.mpesaPollTimer = null;
    }
  }

  private pollForMpesaConfirmation(reference: string, expectedAmount: number, checkoutRequestId?: string): void {
    this.clearMpesaPolling();

    const tenantId = this.auth.currentUser()?.tenantId;
    if (!tenantId) {
      return;
    }

    this.mpesaPollTimer = window.setInterval(() => {
      this.service.getPayments(tenantId, 0, 100).subscribe({
        next: res => {
          const payments = res.data?.content ?? [];
          const match = payments.find((payment: TenantPayment) => {
            if (checkoutRequestId) {
              return payment.externalReference === checkoutRequestId;
            }
            const sameReference = (payment.reference ?? '').trim().toLowerCase() === reference.trim().toLowerCase();
            const sameAmount = Number(payment.amount) === expectedAmount;
            return sameReference && sameAmount && payment.status === 'PENDING';
          });

          if (!match) {
            return;
          }

          const status = (match.status ?? '').toUpperCase();
          if (status === 'COMPLETED') {
            this.clearMpesaPolling();
            this.isAwaitingMpesaConfirmation.set(false);
            this.submitState.set('success');
            this.submitMessage.set('M-Pesa payment completed successfully.');
            this.ngOnInit();
            return;
          }

          if (status === 'FAILED') {
            this.clearMpesaPolling();
            this.isAwaitingMpesaConfirmation.set(false);
            this.submitState.set('error');
            this.submitMessage.set('M-Pesa payment failed. Please try again or contact support.');
            this.ngOnInit();
          }
        },
        error: () => {
          this.clearMpesaPolling();
          this.submitState.set('error');
          this.submitMessage.set('M-Pesa payment is pending confirmation. Please check again shortly.');
        }
      });
    }, 5000);
  }

  submitPayment(): void {
    const tenantId = this.auth.currentUser()?.tenantId;
    const unitId = this.auth.currentUser()?.unitId;

    if (!tenantId) {
      this.submitMessage.set('Your tenant profile is not linked to the system yet.');
      return;
    }

    const numericAmount = Number(this.amount);
    if (!this.amount || Number.isNaN(numericAmount) || numericAmount <= 0) {
      this.submitMessage.set('Please enter a valid amount greater than zero.');
      return;
    }

    if (!this.phoneNumber?.trim() && this.method === 'MPESA') {
      this.submitMessage.set('Please enter the phone number for the payment.');
      return;
    }

    const payload: CreateTenantPaymentRequest = {
      tenantId,
      unitId,
      amount: numericAmount,
      method: this.method as CreateTenantPaymentRequest['method'],
      phoneNumber: this.phoneNumber?.trim() || null,
      reference: this.reference?.trim() || (this.paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment'),
      paymentDate: new Date().toISOString().slice(0, 10)
    };

    this.submitting.set(true);
    this.submitMessage.set(null);
    this.submitState.set(null);

    const request$ = this.method === 'MPESA'
      ? this.service.initiateMpesaStkPush({
          phoneNumber: this.phoneNumber.trim(),
          amount: numericAmount,
          tenantId,
          unitId,
          reference: payload.reference ?? null,
          accountReference: this.paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment',
          transactionDesc: this.paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment'
        })
      : this.service.createPayment(payload);

    request$.subscribe({
      next: response => {
        this.submitting.set(false);

        if (this.method === 'MPESA') {
          const checkoutResponse = response?.data;
          const message = checkoutResponse?.responseDescription || checkoutResponse?.message || 'M-Pesa prompt sent. Please approve the request on your phone to complete the payment.';
          this.isAwaitingMpesaConfirmation.set(true);
          this.submitState.set('success');
          this.submitMessage.set(message + ' Awaiting confirmation…');
          this.pollForMpesaConfirmation(
            payload.reference ?? 'M-Pesa payment',
            numericAmount,
            checkoutResponse?.checkoutRequestId
          );
        } else {
          this.submitState.set('success');
          this.submitMessage.set(`${this.paymentType() === 'rent' ? 'Rent' : 'Security deposit'} payment submitted successfully.`);
        }

        this.amount = '';
        this.phoneNumber = '';
        this.reference = this.paymentType() === 'rent' ? 'Rent payment' : 'Security deposit payment';
        this.ngOnInit();
      },
      error: err => {
        this.submitting.set(false);
        this.submitState.set('error');
        const backendMessage = err?.error?.message || 'Unable to submit the payment right now. Please try again.';
        this.submitMessage.set(backendMessage);
      }
    });
  }

  ngOnInit(): void {
    const tenantId = this.auth.currentUser()?.tenantId;
    if (!tenantId) {
      this.error.set('Your tenant profile is not linked to the system yet.');
      this.loading.set(false);
      return;
    }

    this.service.getPayments(tenantId, 0, 100).subscribe({
      next: res => {
        this.items.set(res.data?.content ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load your payment history right now.');
        this.loading.set(false);
      }
    });
  }
}
