import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from './sidebar.component';
import { TopbarComponent } from './topbar.component';

const COLLAPSED_KEY = 'rf.sidebar.collapsed';

/**
 * Application shell: sidebar, top bar and the routed page.
 *
 * The sidebar behaves differently by viewport. On desktop it is docked and
 * toggles between full width and an icon rail, and that choice is remembered. On
 * handsets it becomes an overlay drawer that closes on navigation, so the rail
 * never eats the content width on a small screen.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, TopbarComponent],
  template: `
    <div class="h-screen flex overflow-hidden bg-gray-100">

      <!-- Docked rail (desktop) -->
      <aside *ngIf="!isHandset()" class="shrink-0 transition-[width] duration-200 ease-out">
        <app-sidebar [collapsed]="collapsed()"></app-sidebar>
      </aside>

      <!-- Overlay drawer (handset) -->
      <ng-container *ngIf="isHandset() && drawerOpen()">
        <div class="fixed inset-0 z-40 bg-black/40"
             (click)="closeDrawer()"
             aria-hidden="true"></div>
        <aside class="fixed inset-y-0 left-0 z-50 shadow-xl">
          <app-sidebar [collapsed]="false" (navigate)="closeDrawer()"></app-sidebar>
        </aside>
      </ng-container>

      <div class="flex-1 flex flex-col min-w-0">
        <app-topbar
          [sidebarCollapsed]="collapsed()"
          (toggleSidebar)="toggleSidebar()">
        </app-topbar>

        <main class="flex-1 overflow-y-auto">
          <div class="p-4 sm:p-6 max-w-7xl mx-auto">
            <router-outlet></router-outlet>
          </div>
        </main>
      </div>
    </div>
  `
})
export class LayoutComponent {
  private breakpointObserver = inject(BreakpointObserver);
  private destroyRef = inject(DestroyRef);

  protected isHandset = signal(false);
  protected drawerOpen = signal(false);

  private collapsedPreference = signal(readCollapsedPreference());

  /** The icon rail is a desktop affordance; the drawer is always full width. */
  protected collapsed = computed(() => !this.isHandset() && this.collapsedPreference());

  constructor() {
    this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.TabletPortrait])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => {
        this.isHandset.set(result.matches);
        if (!result.matches) {
          this.drawerOpen.set(false);
        }
      });
  }

  protected toggleSidebar(): void {
    if (this.isHandset()) {
      this.drawerOpen.update(open => !open);
      return;
    }

    this.collapsedPreference.update(collapsed => {
      const next = !collapsed;
      writeCollapsedPreference(next);
      return next;
    });
  }

  protected closeDrawer(): void {
    this.drawerOpen.set(false);
  }
}

/** localStorage can throw in private modes, so the preference is best-effort. */
function readCollapsedPreference(): boolean {
  try {
    return localStorage.getItem(COLLAPSED_KEY) === 'true';
  } catch {
    return false;
  }
}

function writeCollapsedPreference(collapsed: boolean): void {
  try {
    localStorage.setItem(COLLAPSED_KEY, String(collapsed));
  } catch {
    // Preference is cosmetic; ignore storage failures.
  }
}
