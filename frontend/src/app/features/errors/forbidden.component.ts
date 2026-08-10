import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatButtonModule],
  template: `
    <div class="flex flex-col items-center text-center py-20">
      <mat-icon class="!h-14 !w-14 !text-5xl text-amber-500">lock</mat-icon>
      <h1 class="mt-4 text-2xl font-bold text-gray-900">You don't have access to this page</h1>
      <p class="mt-2 text-gray-600 max-w-md">
        Your role doesn't include this area. If you think that's wrong, ask an
        organization owner to review your permissions.
      </p>
      <a mat-flat-button color="primary" routerLink="/dashboard" class="mt-6">Back to dashboard</a>
    </div>
  `
})
export class ForbiddenComponent {}
