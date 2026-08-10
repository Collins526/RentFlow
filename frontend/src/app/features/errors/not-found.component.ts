import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatButtonModule],
  template: `
    <div class="flex flex-col items-center text-center py-20">
      <mat-icon class="!h-14 !w-14 !text-5xl text-gray-300">travel_explore</mat-icon>
      <h1 class="mt-4 text-2xl font-bold text-gray-900">Page not found</h1>
      <p class="mt-2 text-gray-600 max-w-md">
        The page you're looking for doesn't exist or may have moved.
      </p>
      <a mat-flat-button color="primary" routerLink="/dashboard" class="mt-6">Back to dashboard</a>
    </div>
  `
})
export class NotFoundComponent {}
