import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
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
      [subtitle]="subtitle()">
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
      <div *ngIf="isPlatformAdmin()" class="mb-6 rounded-2xl border border-indigo-100 bg-indigo-50 px-5 py-4">
        <div class="flex items-center gap-3">
          <span class="h-10 w-10 rounded-xl bg-indigo-600 text-white grid place-items-center">
            <mat-icon>insights</mat-icon>
          </span>
          <div>
            <h2 class="font-semibold text-indigo-950">System overview</h2>
            <p class="text-sm text-indigo-700">Live analysis across all active organizations and apartments.</p>
          </div>
        </div>
      </div>

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

      <section *ngIf="!isPlatformAdmin()" class="grid gap-4 lg:grid-cols-12 mb-6">
        <article class="lg:col-span-4 bg-slate-900 text-white rounded-2xl p-5 overflow-hidden relative">
          <div class="relative z-10">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-300">Portfolio occupancy</p>
                <p class="mt-1 text-xs text-slate-400">Live apartment performance</p>
              </div>
              <mat-icon class="text-sky-300">donut_large</mat-icon>
            </div>
            <div class="flex items-center gap-5 mt-5">
              <div class="h-28 w-28 rounded-full grid place-items-center shrink-0"
                   [style.background]="occupancyRing()">
                <div class="h-20 w-20 rounded-full bg-slate-900 grid place-items-center">
                  <span class="text-2xl font-bold">{{ data.occupancyRate }}%</span>
                </div>
              </div>
              <div class="space-y-2 text-sm">
                <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-emerald-400"></span>Occupied <strong class="ml-auto">{{ data.occupiedUnits }}</strong></div>
                <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-sky-400"></span>Reserved <strong class="ml-auto">{{ data.reservedUnits }}</strong></div>
                <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-slate-500"></span>Vacant <strong class="ml-auto">{{ data.vacantUnits }}</strong></div>
              </div>
            </div>
          </div>
          <div class="absolute -right-10 -bottom-16 h-40 w-40 rounded-full border border-white/10"></div>
        </article>

        <article class="lg:col-span-4 bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-gray-500">Revenue performance</p>
              <p class="mt-1 text-xs text-gray-400">Monthly rent collection potential</p>
            </div>
            <span class="h-9 w-9 rounded-lg bg-emerald-50 text-emerald-600 grid place-items-center">
              <mat-icon class="!text-xl">trending_up</mat-icon>
            </span>
          </div>
          <p class="mt-5 text-2xl font-bold text-gray-900">
            {{ data.contractedMonthlyRent | currency:'KES ':'symbol':'1.0-0' }}
          </p>
          <div class="mt-4 h-2 rounded-full bg-gray-100 overflow-hidden">
            <div class="h-full rounded-full bg-emerald-500" [style.width.%]="revenueCaptureRate()"></div>
          </div>
          <div class="flex justify-between mt-2 text-xs text-gray-500">
            <span>{{ revenueCaptureRate() }}% captured</span>
            <span>{{ data.potentialMonthlyRent | currency:'KES ':'symbol':'1.0-0' }} potential</span>
          </div>
        </article>

        <article class="lg:col-span-4 bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-gray-500">Portfolio health</p>
              <p class="mt-1 text-xs text-gray-400">Where attention is needed</p>
            </div>
            <mat-icon class="text-amber-500">monitor_heart</mat-icon>
          </div>
          <div class="space-y-4 mt-5">
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-gray-600">Occupied apartments</span><strong>{{ data.occupiedUnits }}/{{ data.totalUnits }}</strong></div>
              <div class="h-1.5 rounded-full bg-gray-100"><div class="h-full rounded-full bg-indigo-500" [style.width.%]="data.occupancyRate"></div></div>
            </div>
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-gray-600">Upcoming move-ins</span><strong>{{ data.upcomingTenancies }}</strong></div>
              <div class="h-1.5 rounded-full bg-gray-100"><div class="h-full rounded-full bg-sky-500" [style.width.%]="upcomingRate()"></div></div>
            </div>
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-gray-600">Maintenance queue</span><strong>{{ data.unitsUnderMaintenance }}</strong></div>
              <div class="h-1.5 rounded-full bg-gray-100"><div class="h-full rounded-full bg-amber-500" [style.width.%]="maintenanceRate()"></div></div>
            </div>
          </div>
        </article>
      </section>

      <section *ngIf="!isPlatformAdmin()" class="flex flex-wrap items-center gap-3 mb-6">
        <a mat-flat-button color="primary" routerLink="/properties"><mat-icon>add_home</mat-icon> Add property</a>
        <a mat-stroked-button routerLink="/tenancies"><mat-icon>assignment_add</mat-icon> Create tenancy</a>
        <a mat-stroked-button routerLink="/tenants"><mat-icon>person_add</mat-icon> Add tenant</a>
      </section>

      <section *ngIf="isPlatformAdmin()" class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-6">
        <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div>
            <h2 class="font-semibold text-gray-900">Organizations across the system</h2>
            <p class="text-sm text-gray-500 mt-1">Manage every registered organization from one place.</p>
          </div>
          <a mat-stroked-button routerLink="/organization">View all</a>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 text-left text-xs uppercase tracking-wide text-gray-500">
              <tr>
                <th class="px-5 py-3 font-medium">Organization</th>
                <th class="px-5 py-3 font-medium">Properties</th>
                <th class="px-5 py-3 font-medium">Apartments</th>
                <th class="px-5 py-3 font-medium">Tenants</th>
                <th class="px-5 py-3 font-medium">Active tenancies</th>
                <th class="px-5 py-3 font-medium text-right">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr *ngFor="let organization of data.organizations"
                  class="hover:bg-indigo-50 cursor-pointer"
                  (click)="openOrganizationProperties(organization.id)">
                <td class="px-5 py-3">
                  <p class="font-medium text-gray-900">{{ organization.name }}</p>
                  <p class="text-xs text-gray-500">{{ organization.email || 'No contact email' }}</p>
                </td>
                <td class="px-5 py-3 text-gray-600">{{ organization.properties }}</td>
                <td class="px-5 py-3 text-gray-600">{{ organization.units }}</td>
                <td class="px-5 py-3 text-gray-600">{{ organization.tenants }}</td>
                <td class="px-5 py-3 text-gray-600">{{ organization.activeTenancies }}</td>
                <td class="px-5 py-3 text-right">
                  <button mat-icon-button matTooltip="View properties" aria-label="View organization properties">
                    <mat-icon>arrow_forward</mat-icon>
                  </button>
                </td>
              </tr>
              <tr *ngIf="data.organizations.length === 0">
                <td colspan="6" class="px-5 py-8 text-center text-gray-500">No organizations registered yet.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div class="grid gap-4 lg:grid-cols-3">
        <section class="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h2 class="font-semibold text-gray-900">{{ isPlatformAdmin() ? 'Occupancy' : 'Portfolio occupancy analysis' }}</h2>
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
  private router = inject(Router);

  summary = signal<DashboardSummary | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  isPlatformAdmin = computed(() => this.auth.hasRole('PLATFORM_ADMIN'));

  /** Bound into the error state, which takes a callback rather than an output. */
  reload = () => this.load();

  greeting = computed(() => {
    const name = this.auth.currentUser()?.firstName;
    return name ? `Welcome back, ${name}` : 'Dashboard';
  });

  subtitle = computed(() => this.isPlatformAdmin()
    ? 'System-wide performance and portfolio analysis.'
    : "Here's how your portfolio is doing today.");

  tiles = computed<MetricTile[]>(() => {
    const data = this.summary();
    if (!data) {
      return [];
    }

    return [
      ...(this.isPlatformAdmin() ? [{
        label: 'Organizations',
        value: `${data.totalOrganizations}`,
        hint: 'Active organizations',
        icon: 'business',
        tone: 'bg-indigo-50 text-indigo-600',
        route: '/organization'
      }] : []),
      ...(!this.isPlatformAdmin() ? [{
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
      }] : [])
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

  occupancyRing = computed(() => {
    const rate = this.summary()?.occupancyRate ?? 0;
    return `conic-gradient(#34d399 0 ${rate}%, #38bdf8 ${rate}% ${Math.min(100, rate + 12)}%, #475569 ${Math.min(100, rate + 12)}% 100%)`;
  });

  revenueCaptureRate = computed(() => {
    const data = this.summary();
    if (!data || data.potentialMonthlyRent <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((data.contractedMonthlyRent / data.potentialMonthlyRent) * 100));
  });

  upcomingRate = computed(() => {
    const data = this.summary();
    if (!data || data.totalUnits <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((data.upcomingTenancies / data.totalUnits) * 100));
  });

  maintenanceRate = computed(() => {
    const data = this.summary();
    if (!data || data.totalUnits <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((data.unitsUnderMaintenance / data.totalUnits) * 100));
  });

  ngOnInit(): void {
    this.load();
  }

  openOrganizationProperties(organizationId: string): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.router.navigate(['/properties'], { queryParams: { organizationId } });
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
