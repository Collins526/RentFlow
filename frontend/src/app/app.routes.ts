import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./authentication/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./authentication/register/register.component').then(m => m.RegisterComponent) }
];
