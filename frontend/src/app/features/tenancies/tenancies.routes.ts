import { Routes } from '@angular/router';

/**
 * Create and edit both run as dialogs launched from the list, so the feature
 * exposes a single route.
 */
export const TENANCY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./tenancy-list/tenancy-list').then(m => m.TenancyList)
  }
];
