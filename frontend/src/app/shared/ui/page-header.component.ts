import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Title block at the top of a feature page. The `actions` slot takes the
 * page-level buttons so every screen aligns them the same way.
 */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex flex-wrap justify-between items-start gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ title() }}</h1>
        <p *ngIf="subtitle()" class="text-gray-600 mt-1">{{ subtitle() }}</p>
      </div>
      <div class="flex items-center gap-2">
        <ng-content select="[actions]"></ng-content>
      </div>
    </div>
  `
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input('');
}
