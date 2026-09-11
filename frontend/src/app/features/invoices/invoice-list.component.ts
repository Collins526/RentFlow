import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { Tenant } from '../../core/models/tenant.model';
import { Tenancy, TenancyStatus } from '../../core/models/tenancy.model';
import { TenantService } from '../../core/services/tenant/tenant.service';
import { TenancyService } from '../../core/services/tenancy/tenancy.service';
import { InvoiceService, OwnerInvoice } from '../../core/services/invoice/invoice.service';
import { formatEnumLabel } from '../../core/services/unit/unit.service';
import { ToastService, resolveApiMessage } from '../../core/services/toast.service';
import { PageHeaderComponent, StatusChipComponent, statusTone } from '../../shared/ui';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatButtonModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatSelectModule, PageHeaderComponent, StatusChipComponent
  ],
  template: `
    <app-page-header title="Rent invoices" subtitle="Create clear monthly rent bills for your tenants.">
      <button actions mat-flat-button color="primary" type="button" (click)="resetInvoiceForm()" [disabled]="submitting()">
        <mat-icon>add</mat-icon> New invoice
      </button>
    </app-page-header>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
      <section class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-5">
          <p class="text-sm font-semibold uppercase tracking-wide text-indigo-600">Owner action</p>
          <h2 class="mt-1 text-xl font-semibold text-slate-900">Create rent invoice</h2>
          <p class="mt-1 text-sm text-slate-500">The tenant will see the unit, billing month, amount, and payment deadline.</p>
        </div>

        <form [formGroup]="form" class="space-y-4" (ngSubmit)="saveInvoice()">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Tenant</mat-label>
            <mat-select formControlName="tenantId" (selectionChange)="onTenantChange()">
              <mat-option *ngFor="let tenant of tenants()" [value]="tenant.id">{{ tenantName(tenant) }}</mat-option>
            </mat-select>
            <mat-error>Tenant is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Unit</mat-label>
            <mat-select formControlName="tenancyId" (selectionChange)="onTenancyChange()">
              <mat-option *ngFor="let tenancy of selectedTenancies()" [value]="tenancy.id">
                {{ tenancy.unitNumber }}
              </mat-option>
            </mat-select>
            <mat-hint *ngIf="form.get('tenantId')?.value && selectedTenancies().length === 0">This tenant has no active tenancy.</mat-hint>
            <mat-error>Unit is required</mat-error>
          </mat-form-field>

          <div class="grid gap-4 sm:grid-cols-2">
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Billing month</mat-label>
              <input matInput type="month" formControlName="billingMonth">
              <mat-error>Billing month is required</mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Amount (KES)</mat-label>
              <input matInput type="number" min="0" formControlName="amount">
              <mat-error>Enter a valid amount</mat-error>
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Payment deadline</mat-label>
            <input matInput type="date" formControlName="dueDate">
            <mat-error>Payment deadline is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Message to tenant (optional)</mat-label>
            <textarea matInput rows="3" formControlName="notes" placeholder="Rent for the selected billing month"></textarea>
          </mat-form-field>

          <div *ngIf="formError()" class="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">{{ formError() }}</div>
          <button mat-flat-button color="primary" class="w-full" type="submit" [disabled]="form.invalid || submitting()">
            <mat-icon>send</mat-icon> {{ submitting() ? 'Issuing invoice…' : 'Create and issue invoice' }}
          </button>
        </form>
      </section>

      <section>
        <div class="mb-3 flex items-end justify-between gap-3">
          <div>
            <p class="text-sm font-semibold uppercase tracking-wide text-indigo-600">Issued bills</p>
            <h2 class="mt-1 text-xl font-semibold text-slate-900">Invoice history</h2>
          </div>
          <span class="text-sm text-slate-500">{{ invoices().length }} shown</span>
        </div>
        <div *ngIf="loading()" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-500">Loading invoices…</div>
        <div *ngIf="!loading() && invoices().length === 0" class="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-slate-500">No invoices have been issued yet.</div>
        <div *ngFor="let invoice of invoices()" class="mb-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm font-semibold text-slate-900">{{ invoice.tenantName || 'Tenant' }}</p>
              <p class="text-sm text-slate-500">{{ invoice.unitNumber || 'Unit' }} · {{ monthLabel(invoice.periodStart) }}</p>
            </div>
            <div class="text-right">
              <p class="text-lg font-bold text-slate-900">{{ invoice.amount | currency:'KES ':'symbol':'1.0-2' }}</p>
              <app-status-chip [label]="formatLabel(invoiceStatus(invoice.status))" [tone]="statusTone(invoice.status)"></app-status-chip>
            </div>
          </div>
          <div class="mt-3 flex flex-wrap gap-4 text-sm text-slate-500">
            <span>Due {{ invoice.dueDate | date:'mediumDate' }}</span>
            <span>Period {{ invoice.periodStart | date:'mediumDate' }} to {{ invoice.periodEnd | date:'mediumDate' }}</span>
          </div>
        </div>
      </section>
    </div>
  `,
  providers: [CurrencyPipe, DatePipe]
})
export class InvoiceListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly tenantService = inject(TenantService);
  private readonly tenancyService = inject(TenancyService);
  private readonly invoiceService = inject(InvoiceService);
  private readonly toast = inject(ToastService);

  tenants = signal<Tenant[]>([]);
  tenancies = signal<Tenancy[]>([]);
  selectedTenantId = signal('');
  invoices = signal<OwnerInvoice[]>([]);
  loading = signal(true);
  submitting = signal(false);
  formError = signal<string | null>(null);
  readonly formatLabel = formatEnumLabel;
  readonly statusTone = statusTone;
  readonly defaults = { tenantId: '', tenancyId: '', billingMonth: this.currentMonth(), amount: null, dueDate: '', notes: '' };

  form = this.fb.group({
    tenantId: ['', Validators.required],
    tenancyId: ['', Validators.required],
    billingMonth: [this.currentMonth(), Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    dueDate: ['', Validators.required],
    notes: ['']
  });

  selectedTenancies = computed(() => this.tenancies().filter(t => t.tenantId === this.selectedTenantId() && t.status === TenancyStatus.ACTIVE));

  ngOnInit(): void {
    forkJoin({ tenants: this.tenantService.getAllTenants(0, 200), tenancies: this.tenancyService.getTenancies(0, 200, { status: TenancyStatus.ACTIVE }), invoices: this.invoiceService.getInvoices(0, 100) }).subscribe({
      next: result => { this.tenants.set(result.tenants.data?.content ?? []); this.tenancies.set(result.tenancies.data?.content ?? []); this.invoices.set(result.invoices.data?.content ?? []); this.loading.set(false); },
      error: err => { this.formError.set(resolveApiMessage(err, 'Unable to load invoice data.')); this.loading.set(false); }
    });
  }

  tenantName(tenant: Tenant): string { return tenant.tenantType === 'CORPORATE' ? tenant.companyName || tenant.email : `${tenant.firstName || ''} ${tenant.lastName || ''}`.trim() || tenant.email; }
  currency(amount?: number | null): string { return amount == null ? 'Rent not set' : `KES ${amount.toLocaleString()}`; }
  monthLabel(date: string): string { return new Date(`${date}T00:00:00`).toLocaleDateString(undefined, { month: 'long', year: 'numeric' }); }
  invoiceStatus(status: string): string { return status === 'PAID' ? 'SETTLED' : status; }

  onTenantChange(): void {
    const tenantId = this.form.get('tenantId')?.value ?? '';
    this.selectedTenantId.set(tenantId);
    this.form.patchValue({ tenancyId: '', amount: null });
    const tenancy = this.tenancies().find(item => item.tenantId === tenantId && item.status === TenancyStatus.ACTIVE);
    if (tenancy) this.form.patchValue({ tenancyId: tenancy.id });
  }

  onTenancyChange(): void {}

  resetInvoiceForm(): void {
    this.selectedTenantId.set('');
    this.form.reset({ ...this.defaults, billingMonth: this.currentMonth() });
  }

  saveInvoice(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const billingMonth = value.billingMonth;
    if (!billingMonth || !value.dueDate) { return; }
    const start = `${billingMonth}-01`;
    const end = new Date(Number(billingMonth.slice(0, 4)), Number(billingMonth.slice(5, 7)), 0).toISOString().slice(0, 10);
    const tenancy = this.selectedTenancies().find(t => t.id === value.tenancyId);
    if (!tenancy) { this.formError.set('Select an active tenant unit.'); return; }
    this.submitting.set(true); this.formError.set(null);
    this.invoiceService.createInvoice({ tenancyId: tenancy.id, tenantId: value.tenantId!, unitId: tenancy.unitId, periodStart: start, periodEnd: end, dueDate: value.dueDate!, amount: value.amount, notes: value.notes }).subscribe({
      next: created => this.invoiceService.issueInvoice(created.data!.id).subscribe({
        next: issued => { this.invoices.update(items => [issued.data!, ...items]); this.toast.success('Invoice created and issued to the tenant.'); this.submitting.set(false); this.resetInvoiceForm(); },
        error: err => { this.formError.set(resolveApiMessage(err, 'Invoice was created but could not be issued.')); this.submitting.set(false); }
      }),
      error: err => { this.formError.set(resolveApiMessage(err, 'Could not create the invoice.')); this.submitting.set(false); }
    });
  }

  private currentMonth(): string { return new Date().toISOString().slice(0, 7); }
}
