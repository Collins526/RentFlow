import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { NotificationService } from '../../core/services/notification/notification.service';
import { resolveApiMessage, ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-announcement-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>Send an announcement</h2>
    <mat-dialog-content>
      <p class="mb-5 text-sm text-slate-600">
        This announcement will appear in every tenant's portal.
      </p>

      <form [formGroup]="form" class="space-y-3" (ngSubmit)="send()">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Title</mat-label>
          <input matInput maxlength="150" formControlName="title" placeholder="e.g. Building water maintenance">
          <mat-hint align="end">{{ form.controls.title.value.length }}/150</mat-hint>
          <mat-error *ngIf="form.controls.title.hasError('required')">Title is required</mat-error>
          <mat-error *ngIf="form.controls.title.hasError('maxlength')">Title must be 150 characters or fewer</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Message</mat-label>
          <textarea matInput rows="5" maxlength="2000" formControlName="message"
                    placeholder="Write the information you want to share with your tenants"></textarea>
          <mat-hint align="end">{{ form.controls.message.value.length }}/2000</mat-hint>
          <mat-error *ngIf="form.controls.message.hasError('required')">Message is required</mat-error>
          <mat-error *ngIf="form.controls.message.hasError('maxlength')">Message must be 2000 characters or fewer</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close [disabled]="sending()">Cancel</button>
      <button mat-flat-button color="primary" type="button" (click)="send()"
              [disabled]="form.invalid || sending()">
        <mat-icon>campaign</mat-icon>
        {{ sending() ? 'Sending…' : 'Send to all tenants' }}
      </button>
    </mat-dialog-actions>
  `
})
export class AnnouncementDialogComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notificationService = inject(NotificationService);
  private readonly toast = inject(ToastService);
  private readonly dialogRef = inject<MatDialogRef<AnnouncementDialogComponent, number>>(MatDialogRef);

  readonly sending = signal(false);
  readonly form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    message: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  send(): void {
    if (this.form.invalid || this.sending()) {
      this.form.markAllAsTouched();
      return;
    }

    this.sending.set(true);
    this.notificationService.announceToAllTenants(this.form.getRawValue()).subscribe({
      next: response => {
        this.sending.set(false);
        if (!response.success) {
          this.toast.error(response.message || 'Could not send the announcement.');
          return;
        }
        if (response.data === 0) {
          this.toast.warning('There are no active tenants to send this announcement to.');
        } else {
          this.toast.success(`Announcement sent to ${response.data} ${response.data === 1 ? 'tenant' : 'tenants'}.`);
        }
        this.dialogRef.close(response.data);
      },
      error: error => {
        this.sending.set(false);
        this.toast.error(resolveApiMessage(error, 'Could not send the announcement.'));
      }
    });
  }
}
