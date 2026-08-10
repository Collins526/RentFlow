import { Routes } from '@angular/router';

/**
 * Create and edit both run as dialogs launched from the list, so the feature
 * exposes a single route.
 */
export const TENANT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./tenant-list/tenant-list').then(m => m.TenantList)
  }
];
