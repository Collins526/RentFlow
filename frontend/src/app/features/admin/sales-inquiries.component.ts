import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SalesInquiry, SalesInquiryService } from '../../core/services/sales-inquiry.service';

@Component({
  selector: 'app-sales-inquiries',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <section class="mx-auto max-w-6xl">
      <header class="mb-8">
        <p class="text-sm font-semibold uppercase text-indigo-600">Administration</p>
        <h1 class="mt-2 text-3xl font-bold text-gray-900">Sales inquiries</h1>
        <p class="mt-2 text-gray-600">Messages submitted through the public sales form.</p>
      </header>

      <div *ngIf="loading()" class="flex justify-center py-16" aria-label="Loading inquiries">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <div *ngIf="!loading() && error()" class="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
        {{ error() }}
      </div>

      <div *ngIf="!loading() && !error()" class="divide-y divide-gray-200 border-y border-gray-200">
        <article *ngFor="let inquiry of inquiries()" class="grid gap-3 py-5 md:grid-cols-[1fr_2fr_auto] md:items-start">
          <div>
            <h2 class="font-semibold text-gray-900">{{ inquiry.name }}</h2>
            <a class="text-sm text-indigo-700 hover:underline" [href]="'mailto:' + inquiry.email">{{ inquiry.email }}</a>
          </div>
          <p class="whitespace-pre-wrap break-words text-sm leading-relaxed text-gray-700">{{ inquiry.message }}</p>
          <time class="text-xs text-gray-500 md:text-right" [attr.datetime]="inquiry.createdAt">
            {{ inquiry.createdAt | date:'medium' }}
          </time>
        </article>
        <div *ngIf="!inquiries().length" class="py-16 text-center">
          <mat-icon class="!h-9 !w-9 !text-4xl text-gray-400">inbox</mat-icon>
          <p class="mt-3 font-medium text-gray-800">No sales inquiries yet</p>
        </div>
      </div>
    </section>
  `
})
export class SalesInquiriesComponent implements OnInit {
  private readonly service = inject(SalesInquiryService);

  readonly inquiries = signal<SalesInquiry[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.service.list().subscribe({
      next: response => {
        this.inquiries.set(response.data.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Sales inquiries could not be loaded. Try again later.');
        this.loading.set(false);
      }
    });
  }
}