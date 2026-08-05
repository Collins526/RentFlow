import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./authentication/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./authentication/register/register.component').then(m => m.RegisterComponent) },
  { 
    path: 'organization', 
    canActivate: [authGuard],
    loadComponent: () => import('./features/organization/organization-settings.component').then(m => m.OrganizationSettingsComponent) 
  },
  {
    path: 'properties',
    canActivate: [authGuard],
    loadComponent: () => import('./features/properties/property-list.component').then(m => m.PropertyListComponent)
  },
  {
    path: 'properties/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/properties/property-detail.component').then(m => m.PropertyDetailComponent)
  }
];
