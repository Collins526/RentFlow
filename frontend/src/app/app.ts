import { Component, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { RouterOutlet } from '@angular/router';

/**
 * Root component. The application shell lives in LayoutComponent, which is the
 * parent route for every authenticated page; the auth pages render bare.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly document = inject(DOCUMENT);

  constructor() {
    try {
      const theme = localStorage.getItem('rf.theme');
      if (theme === 'light' || theme === 'dark') {
        this.document.documentElement.setAttribute('data-theme', theme);
      }
    } catch {
      // The dark theme remains the default when browser storage is unavailable.
    }
  }
}
