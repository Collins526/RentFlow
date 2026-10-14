import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, concatMap, forkJoin, from, map, of, switchMap, throwError, toArray } from 'rxjs';
import { Tenancy, TenancyStatus } from '../../core/models/tenancy.model';
import { InvoiceService, OwnerInvoice } from '../../core/services/invoice/invoice.service';
import { TenancyService } from '../../core/services/tenancy/tenancy.service';
import { ToastService, resolveApiMessage } from '../../core/services/toast.service';

type InvoiceResult =
  | { invoice: OwnerInvoice; issued: true }
  | { invoice: OwnerInvoice; issued: false; error: string }
  | { issued: false; error: string };

@Component({
  selector: 'app-bulk-invoice-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>Create multiple invoices</h2>
    <mat-dialog-content>
      <p class="mb-5 text-sm text-slate-600">
        Issue one invoice to each active tenancy ({{ tenancies().length }} total). Each invoice uses that tenancy's rent amount.
      </p>

      <div *ngIf="loading()" class="py-6 text-sm text-slate-500">Loading active tenancies…</div>
      <div *ngIf="loadError()" class="mb-4 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ loadError() }}
      </div>

      <form *ngIf="!loading() && !loadError()" [formGroup]="form" class="space-y-4" (ngSubmit)="createInvoices()">
        <div *ngIf="tenancies().length === 0" class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          There are no active tenancies to invoice.
        </div>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Billing month</mat-label>
          <input matInput type="month" formControlName="billingMonth">
          <mat-error>Billing month is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Payment deadline</mat-label>
          <input matInput type="date" formControlName="dueDate">
          <mat-error>Payment deadline is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Message to tenants (optional)</mat-label>
          <textarea matInput rows="3" formControlName="notes" placeholder="Rent for the selected billing month"></textarea>
        </mat-form-field>
      </form>

      <div *ngIf="batchSummary()" class="mb-4 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">
        {{ batchSummary() }}
      </div>
      <ul *ngIf="failures().length > 0" class="mb-4 list-inside list-disc space-y-1 text-sm text-rose-700">
        <li *ngFor="let failure of failures()">{{ failure }}</li>
      </ul>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="close()" [disabled]="submitting()">
        {{ failures().length > 0 || batchSummary() ? 'Close' : 'Cancel' }}
      </button>
      <button
        *ngIf="!batchSummary()"
        mat-flat-button
        color="primary"
        type="button"
        (click)="createInvoices()"
        [disabled]="loading() || !!loadError() || form.invalid || submitting() || tenancies().length === 0">
        <mat-icon>send</mat-icon>
        {{ submitting() ? 'Issuing invoices…' : 'Create and issue invoices' }}
      </button>
    </mat-dialog-actions>
  `
})
export class BulkInvoiceFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly tenancyService = inject(TenancyService);
  private readonly invoiceService = inject(InvoiceService);
  private readonly toast = inject(ToastService);
  private readonly dialogRef = inject<MatDialogRef<BulkInvoiceFormComponent, OwnerInvoice[]>>(MatDialogRef);

  tenancies = signal<Tenancy[]>([]);
  failures = signal<string[]>([]);
  loading = signal(true);
  submitting = signal(false);
  loadError = signal<string | null>(null);
  batchSummary = signal<string | null>(null);
  private createdInvoices: OwnerInvoice[] = [];

  form = this.fb.group({
    billingMonth: [new Date().toISOString().slice(0, 7), Validators.required],
    dueDate: ['', Validators.required],
    notes: ['']
  });

  ngOnInit(): void {
    this.loadActiveTenancies();
  }

  createInvoices(): void {
    if (this.form.invalid || this.submitting() || this.tenancies().length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const billingMonth = value.billingMonth;
    if (!billingMonth || !value.dueDate) {
      return;
    }

    const periodStart = `${billingMonth}-01`;
    const periodEnd = this.monthEnd(billingMonth);
    const tenancies = this.tenancies();
    this.submitting.set(true);

    from(tenancies).pipe(
      concatMap(tenancy => this.invoiceService.createInvoice({
        tenancyId: tenancy.id,
        tenantId: tenancy.tenantId,
        unitId: tenancy.unitId,
        periodStart,
        periodEnd,
        dueDate: value.dueDate!,
        amount: tenancy.rentAmount,
        notes: value.notes
      }).pipe(
        switchMap(created => {
          if (!created.success || !created.data?.id) {
            return throwError(() => new Error(created.message || 'Could not create the invoice.'));
          }
          return this.invoiceService.issueInvoice(created.data.id).pipe(
            map(issued => {
              if (!issued.success || !issued.data) {
                throw new Error(issued.message || 'Could not issue the invoice.');
              }
              return { invoice: issued.data, issued: true } satisfies InvoiceResult;
            }),
            catchError(error => of({
              invoice: created.data!,
              issued: false,
              error: `${tenancy.tenantName} - ${tenancy.unitNumber}: Invoice was created as a draft but could not be issued. ${resolveApiMessage(error, 'Issue request failed.')}`
            } satisfies InvoiceResult))
          );
        }),
        catchError(error => of({
          issued: false,
          error: `${tenancy.tenantName} - ${tenancy.unitNumber}: ${resolveApiMessage(error, 'Invoice could not be created and issued.')}`
        } satisfies InvoiceResult))
      )),
      toArray()
    ).subscribe(results => {
      this.createdInvoices = results.flatMap(result => 'invoice' in result ? [result.invoice] : []);
      const issuedCount = results.filter(result => result.issued).length;
      const failures = results.flatMap(result => 'error' in result ? [result.error] : []);
      this.failures.set(failures);
      this.submitting.set(false);

      if (failures.length > 0) {
        this.batchSummary.set(`Issued ${issuedCount} of ${tenancies.length} invoices.`);
        this.toast.warning(`Issued ${issuedCount} of ${tenancies.length} invoices. Review the errors for details.`);
        return;
      }

      this.toast.success(`Created and issued ${issuedCount} invoices.`);
      this.dialogRef.close(this.createdInvoices);
    });
  }

  close(): void {
    this.dialogRef.close(this.createdInvoices);
  }

  private loadActiveTenancies(): void {
    this.tenancyService.getTenancies(0, 200, { status: TenancyStatus.ACTIVE }).pipe(
      switchMap(firstPage => {
        if (!firstPage.success || !firstPage.data) {
          return throwError(() => new Error(firstPage.message || 'Unable to load active tenancies.'));
        }

        const remainingPages = Array.from(
          { length: Math.max(0, firstPage.data.totalPages - 1) },
          (_, index) => index + 1
        );
        if (remainingPages.length === 0) {
          return of(firstPage.data.content);
        }

        return forkJoin(remainingPages.map(page =>
          this.tenancyService.getTenancies(page, 200, { status: TenancyStatus.ACTIVE })
        )).pipe(
          map(pages => {
            const failedPage = pages.find(page => !page.success || !page.data);
            if (failedPage) {
              throw new Error(failedPage.message || 'Unable to load all active tenancies.');
            }
            return [
              ...firstPage.data.content,
              ...pages.flatMap(page => page.data.content)
            ];
          })
        );
      })
    ).subscribe({
      next: tenancies => {
        this.tenancies.set(tenancies.filter(tenancy => tenancy.status === TenancyStatus.ACTIVE));
        this.loading.set(false);
      },
      error: error => {
        this.loadError.set(resolveApiMessage(error, 'Unable to load active tenancies.'));
        this.loading.set(false);
      }
    });
  }

  private monthEnd(month: string): string {
    const [year, monthNumber] = month.split('-').map(Number);
    const lastDay = new Date(year, monthNumber, 0).getDate();
    return `${month}-${String(lastDay).padStart(2, '0')}`;
  }
}
