import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type StatusTone = 'success' | 'info' | 'warning' | 'danger' | 'neutral';

const TONE_CLASSES: Record<StatusTone, string> = {
  success: 'bg-green-100 text-green-800',
  info: 'bg-blue-100 text-blue-800',
  warning: 'bg-amber-100 text-amber-800',
  danger: 'bg-red-100 text-red-800',
  neutral: 'bg-gray-100 text-gray-800'
};

/**
 * Pill for an enum-ish status. Centralised so ACTIVE is the same green in every
 * table, rather than each list re-deriving its own `ngClass` map.
 */
@Component({
  selector: 'app-status-chip',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="inline-block px-2.5 py-1 rounded-full text-xs font-medium whitespace-nowrap"
          [ngClass]="toneClass()">
      {{ label() }}
    </span>
  `
})
export class StatusChipComponent {
  label = input.required<string>();
  tone = input<StatusTone>('neutral');

  protected toneClass = computed(() => TONE_CLASSES[this.tone()]);
}

/** Shared status-to-tone mapping for the statuses used across the app. */
export function statusTone(status: string | null | undefined): StatusTone {
  switch (status) {
    case 'ACTIVE':
    case 'OCCUPIED':
      return 'success';
    case 'UPCOMING':
    case 'RESERVED':
      return 'info';
    case 'UNDER_MAINTENANCE':
      return 'warning';
    case 'EVICTED':
      return 'danger';
    default:
      return 'neutral';
  }
}
