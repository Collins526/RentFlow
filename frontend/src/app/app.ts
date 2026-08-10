import { Component } from '@angular/core';
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
export class App {}
