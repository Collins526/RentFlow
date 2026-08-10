import { Component, computed, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRippleModule } from '@angular/material/core';
import { AuthService } from '../core/services/auth.service';
import { NAV_SECTIONS, NavSection } from '../core/navigation/navigation';

/**
 * Primary navigation.
 *
 * Sections and items are filtered against the signed-in user's roles, so a user
 * is never shown a destination the backend would refuse. Collapsing narrows the
 * rail to icons only; the labels are replaced by tooltips so it stays usable.
 */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, MatIconModule, MatTooltipModule, MatRippleModule],
  template: `
    <nav class="h-full flex flex-col bg-slate-900 text-slate-300"
         [class.w-64]="!collapsed()"
         [class.w-20]="collapsed()"
         aria-label="Main navigation">

      <div class="flex items-center gap-3 h-16 px-5 shrink-0 border-b border-white/5">
        <div class="h-9 w-9 shrink-0 rounded-lg bg-indigo-500 grid place-items-center">
          <mat-icon class="!text-white !text-xl !h-5 !w-5">holiday_village</mat-icon>
        </div>
        <span *ngIf="!collapsed()" class="text-lg font-semibold text-white tracking-tight">RentFlow</span>
      </div>

      <div class="flex-1 overflow-y-auto py-4">
        <div *ngFor="let section of visibleSections()" class="mb-5">
          <p *ngIf="section.heading && !collapsed()"
             class="px-5 mb-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-500">
            {{ section.heading }}
          </p>
          <!-- Keeps the icon rail visually grouped once the headings are hidden. -->
          <div *ngIf="section.heading && collapsed()" class="mx-5 mb-2 border-t border-white/5"></div>

          <a *ngFor="let item of section.items"
             [routerLink]="item.route"
             routerLinkActive="!bg-indigo-500/15 !text-white before:opacity-100"
             [routerLinkActiveOptions]="{ exact: !item.matchPrefix }"
             matRipple
             [matTooltip]="collapsed() ? item.label : ''"
             matTooltipPosition="right"
             (click)="navigate.emit()"
             class="relative flex items-center gap-3 mx-2 px-3 py-2.5 rounded-lg text-sm font-medium
                    text-slate-300 hover:bg-white/5 hover:text-white transition-colors
                    before:absolute before:left-0 before:top-1/2 before:-translate-y-1/2
                    before:h-5 before:w-1 before:rounded-r before:bg-indigo-400 before:opacity-0
                    before:transition-opacity"
             [class.justify-center]="collapsed()">
            <mat-icon class="!h-5 !w-5 !text-xl shrink-0">{{ item.icon }}</mat-icon>
            <span *ngIf="!collapsed()" class="truncate">{{ item.label }}</span>
          </a>
        </div>
      </div>

      <div class="shrink-0 border-t border-white/5 p-3">
        <p *ngIf="!collapsed()" class="px-2 text-[11px] text-slate-500">
          RentFlow &middot; Property Management
        </p>
      </div>
    </nav>
  `
})
export class SidebarComponent {
  private auth = inject(AuthService);

  collapsed = input(false);

  /** Lets the shell close the mobile drawer once a destination is chosen. */
  navigate = output<void>();

  /**
   * Drops items the user cannot reach, then drops sections left empty, so no
   * orphaned heading remains.
   */
  visibleSections = computed<NavSection[]>(() =>
    NAV_SECTIONS
      .map(section => ({
        ...section,
        items: section.items.filter(item => this.auth.hasAnyRole(item.roles))
      }))
      .filter(section => section.items.length > 0));
}
