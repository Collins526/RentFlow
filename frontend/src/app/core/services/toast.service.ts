import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

/**
 * Application-wide toasts.
 *
 * Wraps MatSnackBar so features never repeat panel classes or durations, and so
 * {@link ToastService.apiError} can pull the message out of the backend's
 * `ApiResponse` envelope in one place.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private snackBar = inject(MatSnackBar);

  private readonly base: MatSnackBarConfig = {
    horizontalPosition: 'right',
    verticalPosition: 'top'
  };

  success(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      ...this.base,
      duration: 4000,
      panelClass: ['rf-toast', 'rf-toast-success']
    });
  }

  info(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      ...this.base,
      duration: 4000,
      panelClass: ['rf-toast', 'rf-toast-info']
    });
  }

  warning(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      ...this.base,
      duration: 6000,
      panelClass: ['rf-toast', 'rf-toast-warning']
    });
  }

  /** Errors stay until dismissed — they usually need the user to do something. */
  error(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      ...this.base,
      panelClass: ['rf-toast', 'rf-toast-error']
    });
  }

  /**
   * Surfaces a failed HTTP call. Prefers the field-level `errors` array from bean
   * validation, then the envelope's `message`, then a generic fallback.
   */
  apiError(err: unknown, fallback = 'Something went wrong. Please try again.'): void {
    this.error(resolveApiMessage(err, fallback));
  }
}

/** Exported so components can reuse the same extraction for inline error panels. */
export function resolveApiMessage(err: unknown, fallback: string): string {
  const body = (err as { error?: { message?: string; errors?: string[] } })?.error;

  if (body?.errors?.length) {
    return body.errors.join(', ');
  }

  return body?.message || fallback;
}
