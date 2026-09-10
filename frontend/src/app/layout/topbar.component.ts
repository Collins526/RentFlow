import { Component, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../core/services/auth.service';
import { BreadcrumbsComponent } from './breadcrumbs.component';
import { Role } from '../core/auth/roles';

/**
 * Top bar: sidebar toggle, breadcrumbs and the user profile menu.
 */
@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
    BreadcrumbsComponent
  ],
  template: `
    <header class="h-16 shrink-0 bg-white border-b border-gray-200 flex items-center gap-3 px-4 sm:px-6">
      <button mat-icon-button
              (click)="toggleSidebar.emit()"
              [matTooltip]="sidebarCollapsed() ? 'Expand menu' : 'Collapse menu'"
              aria-label="Toggle navigation">
        <mat-icon>menu</mat-icon>
      </button>

      <app-breadcrumbs class="hidden sm:block"></app-breadcrumbs>

      <div class="ml-auto flex items-center gap-1">
        <button mat-icon-button
                class="!hidden sm:!inline-flex"
                matTooltip="Notifications are coming in a later module"
                aria-label="Notifications"
                disabled>
          <mat-icon>notifications_none</mat-icon>
        </button>

        <button mat-button [matMenuTriggerFor]="profileMenu" class="!px-2" aria-label="Account menu">
          <div class="flex items-center gap-2">
            <span class="h-8 w-8 rounded-full bg-indigo-100 text-indigo-700 grid place-items-center
                         text-xs font-semibold">
              {{ auth.initials() }}
            </span>
            <span class="hidden md:flex flex-col items-start leading-tight">
              <span class="text-sm font-medium text-gray-900">{{ auth.displayName() }}</span>
              <span class="text-[11px] text-gray-500">{{ auth.primaryRoleLabel() }}</span>
            </span>
            <mat-icon class="!h-5 !w-5 !text-lg text-gray-400">expand_more</mat-icon>
          </div>
        </button>

        <mat-menu #profileMenu="matMenu" xPosition="before">
          <div class="px-4 py-3 min-w-56">
            <p class="text-sm font-medium text-gray-900">{{ auth.displayName() }}</p>
            <p class="text-xs text-gray-500 truncate">{{ auth.currentUser()?.email }}</p>
          </div>
          <mat-divider></mat-divider>

          <a *ngIf="canManageOrganization()" mat-menu-item routerLink="/organization">
            <mat-icon>business</mat-icon>
            <span>Organization settings</span>
          </a>

          <mat-divider></mat-divider>
          <button mat-menu-item (click)="auth.logout()">
            <mat-icon>logout</mat-icon>
            <span>Sign out</span>
          </button>
        </mat-menu>
      </div>
    </header>
  `
})
export class TopbarComponent {
  protected auth = inject(AuthService);

  sidebarCollapsed = input(false);
  toggleSidebar = output<void>();

  protected canManageOrganization(): boolean {
    return this.auth.hasRole(Role.OrganizationOwner) && !this.auth.hasRole(Role.PlatformAdmin);
  }
}
