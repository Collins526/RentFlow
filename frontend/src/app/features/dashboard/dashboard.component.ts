import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { DashboardService, DashboardSummary } from '../../core/services/dashboard/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { resolveApiMessage } from '../../core/services/toast.service';
import {
  ErrorStateComponent,
  PageHeaderComponent,
  SkeletonComponent
} from '../../shared/ui';

interface MetricTile {
  label: string;
  value: string;
  icon: string;
  /** Tailwind classes for the icon chip. */
  tone: string;
  hint?: string;
  route?: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    PageHeaderComponent,
    SkeletonComponent,
    ErrorStateComponent
  ],
  template: `
    <app-page-header
      [title]="greeting()"
      subtitle="Here's how your portfolio is doing today.">
      <button actions mat-stroked-button (click)="load()" [disabled]="isLoading()">
        <mat-icon>refresh</mat-icon>
        Refresh
      </button>
    </app-page-header>

    <app-error-state
      *ngIf="error()"
      class="block mb-6"
      [message]="error()!"
      [retry]="reload">
    </app-error-state>

    <!-- Skeleton tiles keep the grid from jumping once the numbers land. -->
    <div *ngIf="isLoading()" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4 mb-6">
      <div *ngFor="let placeholder of [1, 2, 3, 4]"
           class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <app-skeleton [lines]="3"></app-skeleton>
      </div>
    </div>

    <ng-container *ngIf="!isLoading() && summary() as data">
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4 mb-6">
        <a *ngFor="let tile of tiles()"
           [routerLink]="tile.route"
           class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 block transition-shadow"
           [class.hover:shadow-md]="!!tile.route"
           [class.cursor-default]="!tile.route">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ tile.label }}</p>
              <p class="mt-1 text-2xl font-bold text-gray-900">{{ tile.value }}</p>
              <p *ngIf="tile.hint" class="mt-0.5 text-xs text-gray-400">{{ tile.hint }}</p>
            </div>
            <span class="h-10 w-10 shrink-0 rounded-xl grid place-items-center" [ngClass]="tile.tone">
              <mat-icon class="!h-5 !w-5 !text-xl">{{ tile.icon }}</mat-icon>
            </span>
          </div>
        </a>
      </div>

      <div class="grid gap-4 lg:grid-cols-3">
        <section class="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h2 class="font-semibold text-gray-900">Occupancy</h2>
          <p class="text-sm text-gray-500 mb-4">
            {{ data.occupiedUnits }} of {{ data.totalUnits }} units are currently let.
          </p>

          <div class="flex items-baseline gap-2 mb-2">
            <span class="text-3xl font-bold text-gray-900">{{ data.occupancyRate }}%</span>
            <span class="text-sm text-gray-500">occupied</span>
          </div>
          <mat-progress-bar mode="determinate" [value]="data.occupancyRate" class="!h-2 !rounded-full">
          </mat-progress-bar>

          <dl class="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-6">
            <div *ngFor="let bucket of occupancyBuckets()">
              <dt class="text-xs text-gray-500">{{ bucket.label }}</dt>
              <dd class="text-lg font-semibold" [ngClass]="bucket.tone">{{ bucket.count }}</dd>
            </div>
          </dl>
        </section>

        <section class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h2 class="font-semibold text-gray-900">Monthly rent roll</h2>
          <p class="text-sm text-gray-500 mb-4">Contracted against full occupancy.</p>

          <div class="space-y-4">
            <div>
              <p class="text-xs text-gray-500">Contracted</p>
              <p class="text-xl font-bold text-gray-900">
                {{ data.contractedMonthlyRent | currency:'KES ':'symbol':'1.0-0' }}
              </p>
            </div>
            <div>
              <p class="text-xs text-gray-500">Potential at full occupancy</p>
              <p class="text-xl font-semibold text-gray-500">
                {{ data.potentialMonthlyRent | currency:'KES ':'symbol':'1.0-0' }}
              </p>
            </div>
            <div class="pt-3 border-t border-gray-100">
              <p class="text-xs text-gray-500">Vacancy cost</p>
              <p class="text-lg font-semibold text-amber-600">
                {{ vacancyCost() | currency:'KES ':'symbol':'1.0-0' }}
              </p>
            </div>
          </div>
        </section>
      </div>

      <section *ngIf="data.totalProperties === 0"
               class="mt-6 bg-indigo-50 border border-indigo-100 rounded-2xl p-6 text-center">
        <mat-icon class="!h-10 !w-10 !text-4xl text-indigo-400">apartment</mat-icon>
        <h2 class="mt-2 font-semibold text-gray-900">Start with your first property</h2>
        <p class="text-sm text-gray-600 mt-1 mb-4">
          Add a property, then its units, and you can begin placing tenants.
        </p>
        <a mat-flat-button color="primary" routerLink="/properties">Add a property</a>
      </section>
    </ng-container>
  `
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private auth = inject(AuthService);

  summary = signal<DashboardSummary | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  /** Bound into the error state, which takes a callback rather than an output. */
  reload = () => this.load();

  greeting = computed(() => {
    const name = this.auth.currentUser()?.firstName;
    return name ? `Welcome back, ${name}` : 'Dashboard';
  });

  tiles = computed<MetricTile[]>(() => {
    const data = this.summary();
    if (!data) {
      return [];
    }

    return [
      {
        label: 'Properties',
        value: `${data.totalProperties}`,
        hint: data.totalBlocks > 0 ? `${data.totalBlocks} blocks` : undefined,
        icon: 'apartment',
        tone: 'bg-indigo-50 text-indigo-600',
        route: '/properties'
      },
      {
        label: 'Units',
        value: `${data.totalUnits}`,
        hint: `${data.vacantUnits} vacant`,
        icon: 'meeting_room',
        tone: 'bg-sky-50 text-sky-600'
      },
      {
        label: 'Active tenancies',
        value: `${data.activeTenancies}`,
        hint: data.upcomingTenancies > 0 ? `${data.upcomingTenancies} upcoming` : undefined,
        icon: 'assignment_ind',
        tone: 'bg-emerald-50 text-emerald-600',
        route: '/tenancies'
      },
      {
        label: 'Tenants',
        value: `${data.totalTenants}`,
        icon: 'people',
        tone: 'bg-violet-50 text-violet-600',
        route: '/tenants'
      }
    ];
  });

  occupancyBuckets = computed(() => {
    const data = this.summary();
    if (!data) {
      return [];
    }

    return [
      { label: 'Occupied', count: data.occupiedUnits, tone: 'text-emerald-600' },
      { label: 'Vacant', count: data.vacantUnits, tone: 'text-gray-900' },
      { label: 'Reserved', count: data.reservedUnits, tone: 'text-sky-600' },
      { label: 'Maintenance', count: data.unitsUnderMaintenance, tone: 'text-amber-600' }
    ];
  });

  /** Rent the portfolio is not earning because units are not let. */
  vacancyCost = computed(() => {
    const data = this.summary();
    if (!data) {
      return 0;
    }
    return Math.max(0, data.potentialMonthlyRent - data.contractedMonthlyRent);
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.dashboardService.getSummary().subscribe({
      next: res => {
        this.summary.set(res.data);
        this.isLoading.set(false);
      },
      error: err => {
        this.error.set(resolveApiMessage(err, 'Could not load your dashboard.'));
        this.isLoading.set(false);
      }
    });
  }
}
