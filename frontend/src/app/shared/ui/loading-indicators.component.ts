import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

/** Centred spinner for a whole page or panel that has nothing to show yet. */
@Component({
  selector: 'app-loading-state',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="flex flex-col items-center justify-center gap-3" [style.min-height.px]="minHeight()">
      <mat-spinner [diameter]="diameter()"></mat-spinner>
      <p *ngIf="label()" class="text-sm text-gray-500">{{ label() }}</p>
    </div>
  `
})
export class LoadingStateComponent {
  label = input('Loading…');
  diameter = input(36);
  minHeight = input(200);
}

/**
 * Grey placeholder bars for content whose shape is known before its data is.
 * Preferred over a spinner where it avoids a layout jump, e.g. dashboard tiles.
 */
@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="animate-pulse space-y-2" [attr.aria-busy]="true" aria-label="Loading">
      <div *ngFor="let line of lineArray()"
           class="bg-gray-200 rounded"
           [style.height.px]="lineHeight()"
           [style.width]="line"></div>
    </div>
  `
})
export class SkeletonComponent {
  lines = input(3);
  lineHeight = input(12);

  /** Varies the last line's width so the block reads as text rather than a bar chart. */
  protected lineArray(): string[] {
    const count = this.lines();
    return Array.from({ length: count }, (_, index) => (index === count - 1 ? '60%' : '100%'));
  }
}

/** Placeholder for a list or panel that loaded successfully but has no rows. */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="flex flex-col items-center gap-2 py-12 px-6 text-center">
      <mat-icon class="!h-10 !w-10 !text-4xl text-gray-300">{{ icon() }}</mat-icon>
      <p class="text-gray-900 font-medium">{{ title() }}</p>
      <p *ngIf="message()" class="text-sm text-gray-500 max-w-sm">{{ message() }}</p>
      <div class="mt-2">
        <ng-content></ng-content>
      </div>
    </div>
  `
})
export class EmptyStateComponent {
  icon = input('inbox');
  title = input.required<string>();
  message = input('');
}

/** Inline failure panel with a retry affordance. */
@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    <div class="flex items-center gap-3 rounded-xl bg-red-50 border border-red-200 px-4 py-3">
      <mat-icon class="text-red-600">error_outline</mat-icon>
      <span class="text-sm text-red-800 flex-1">{{ message() }}</span>
      <button *ngIf="retryable()" mat-stroked-button color="warn" (click)="onRetry()">Retry</button>
    </div>
  `
})
export class ErrorStateComponent {
  message = input.required<string>();
  retryable = input(true);
  retry = input<(() => void) | null>(null);

  protected onRetry(): void {
    this.retry()?.();
  }
}
