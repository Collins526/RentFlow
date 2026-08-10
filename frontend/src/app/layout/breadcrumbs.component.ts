import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';

export interface Breadcrumb {
  label: string;
  /** Absent for the current page, which is rendered as plain text. */
  url?: string;
}

/**
 * Trail built from each route's `data.breadcrumb`. Routes without that key are
 * skipped, so a layout or wrapper route does not add an empty crumb.
 */
@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  template: `
    <nav *ngIf="crumbs().length > 0" aria-label="Breadcrumb">
      <ol class="flex items-center gap-1 text-sm">
        <li *ngFor="let crumb of crumbs(); let last = last" class="flex items-center gap-1">
          <a *ngIf="!last && crumb.url; else plainCrumb"
             [routerLink]="crumb.url"
             class="text-gray-500 hover:text-indigo-600 transition-colors">
            {{ crumb.label }}
          </a>
          <ng-template #plainCrumb>
            <span class="font-medium text-gray-900" [attr.aria-current]="last ? 'page' : null">
              {{ crumb.label }}
            </span>
          </ng-template>

          <mat-icon *ngIf="!last" class="!h-4 !w-4 !text-base text-gray-300">chevron_right</mat-icon>
        </li>
      </ol>
    </nav>
  `
})
export class BreadcrumbsComponent {
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  crumbs = signal<Breadcrumb[]>([]);

  constructor() {
    this.crumbs.set(this.build(this.route.snapshot));

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntilDestroyed())
      .subscribe(() => this.crumbs.set(this.build(this.route.snapshot)));
  }

  /**
   * Walks from the root down the primary outlet, accumulating URL segments so a
   * crumb links to its own level rather than the leaf.
   */
  private build(root: ActivatedRouteSnapshot): Breadcrumb[] {
    const crumbs: Breadcrumb[] = [];
    let url = '';
    let node: ActivatedRouteSnapshot | undefined = root;

    while (node) {
      const segment = node.url.map(part => part.path).join('/');
      if (segment) {
        url += `/${segment}`;
      }

      const label = node.data?.['breadcrumb'];
      if (label) {
        crumbs.push({ label, url });
      }

      node = node.children.find(child => child.outlet === 'primary');
    }

    // The last crumb is the current page, so it should not be a link.
    if (crumbs.length > 0) {
      delete crumbs[crumbs.length - 1].url;
    }

    return crumbs;
  }
}
