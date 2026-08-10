import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Row of filter controls above a list. Controls are projected by the feature —
 * this owns only the layout, the result count and the clear affordance, so every
 * list's filter row lines up and behaves consistently.
 */
@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div class="flex flex-wrap items-center gap-3 mb-4">
      <ng-content></ng-content>

      <button *ngIf="showClear()" mat-button (click)="clear.emit()" class="!text-gray-600">
        <mat-icon>close</mat-icon>
        Clear filters
      </button>

      <span *ngIf="resultLabel()" class="ml-auto text-sm text-gray-500">{{ resultLabel() }}</span>
    </div>
  `
})
export class FilterBarComponent {
  /** Shown only when at least one filter is applied. */
  showClear = input(false);
  resultLabel = input('');

  clear = output<void>();
}
