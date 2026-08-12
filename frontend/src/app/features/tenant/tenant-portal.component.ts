import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

interface PortalCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
}

@Component({
  selector: 'app-tenant-portal',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="space-y-6 p-6">
      <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex items-center gap-4">
          <div class="h-14 w-14 rounded-2xl bg-indigo-500 grid place-items-center text-white">
            <mat-icon class="!text-3xl">home</mat-icon>
          </div>
          <div>
            <h1 class="text-2xl font-semibold text-slate-900">Tenant portal</h1>
            <p class="mt-1 text-sm text-slate-500">
              Welcome to your tenant dashboard. Manage your leases, invoices, payments, and maintenance requests in one place.
            </p>
          </div>
        </div>
      </div>

      <section class="grid gap-4 md:grid-cols-3">
        <a *ngFor="let card of cards()"
           [routerLink]="card.route"
           class="block rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
          <div class="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl" [ngClass]="card.color">
            <mat-icon>{{ card.icon }}</mat-icon>
          </div>
          <h2 class="text-lg font-semibold text-slate-900">{{ card.title }}</h2>
          <p class="mt-2 text-sm text-slate-500">{{ card.description }}</p>
        </a>
      </section>

      <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">Your tenancy</h2>
        <p class="mt-2 text-sm text-slate-500">
          Your tenant account is active and linked to unit {{ auth.currentUser()?.unitId ? auth.currentUser()!.unitId!.slice(0, 8) : 'N/A' }}.
        </p>
      </section>

      <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">Account</h2>
        <p class="mt-2 text-sm text-slate-500">You are signed in as <strong>{{ auth.displayName() }}</strong>.</p>
        <p class="mt-3 text-sm text-slate-500">If you need help, contact your property manager or organization owner.</p>
        <button mat-flat-button color="primary" (click)="logout()" class="mt-4">Sign out</button>
      </section>
    </div>
  `
})
export class TenantPortalComponent {
  auth = inject(AuthService);

  cards = computed<PortalCard[]>(() => [
    {
      title: 'Invoices',
      description: 'Review issued rent invoices and current due dates.',
      icon: 'receipt_long',
      route: '/tenant/invoices',
      color: 'bg-indigo-100 text-indigo-700'
    },
    {
      title: 'Payments',
      description: 'Track your payment history and references.',
      icon: 'payments',
      route: '/tenant/payments',
      color: 'bg-emerald-100 text-emerald-700'
    },
    {
      title: 'Requests',
      description: 'Submit and monitor maintenance issues for your unit.',
      icon: 'build',
      route: '/tenant/requests',
      color: 'bg-amber-100 text-amber-700'
    }
  ]);

  logout(): void {
    this.auth.logout();
  }
}
