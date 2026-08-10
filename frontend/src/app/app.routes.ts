import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { PORTFOLIO_READ_ROLES, Role } from './core/auth/roles';

/**
 * Everything behind the shell is a child of the layout route, so the sidebar,
 * top bar and breadcrumbs are declared once. Auth pages sit outside it.
 *
 * The `roleGuard`s mirror the `@PreAuthorize` rules on the matching controllers.
 * They are a usability layer over the real check, not a substitute for it.
 */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./authentication/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./authentication/register/register.component').then(m => m.RegisterComponent)
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout.component').then(m => m.LayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        canActivate: [roleGuard(PORTFOLIO_READ_ROLES)],
        data: { breadcrumb: 'Dashboard' },
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'properties',
        canActivate: [roleGuard(PORTFOLIO_READ_ROLES)],
        data: { breadcrumb: 'Properties' },
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/properties/property-list.component').then(m => m.PropertyListComponent)
          },
          {
            path: ':id',
            data: { breadcrumb: 'Property details' },
            loadComponent: () =>
              import('./features/properties/property-detail.component').then(m => m.PropertyDetailComponent)
          }
        ]
      },
      {
        path: 'blocks/:id',
        canActivate: [roleGuard(PORTFOLIO_READ_ROLES)],
        data: { breadcrumb: 'Block details' },
        loadComponent: () =>
          import('./features/blocks/block-detail.component').then(m => m.BlockDetailComponent)
      },
      {
        path: 'tenants',
        canActivate: [roleGuard(PORTFOLIO_READ_ROLES)],
        data: { breadcrumb: 'Tenants' },
        loadChildren: () => import('./features/tenants/tenants.routes').then(m => m.TENANT_ROUTES)
      },
      {
        path: 'tenancies',
        canActivate: [roleGuard(PORTFOLIO_READ_ROLES)],
        data: { breadcrumb: 'Tenancies' },
        loadChildren: () => import('./features/tenancies/tenancies.routes').then(m => m.TENANCY_ROUTES)
      },
      {
        path: 'organization',
        canActivate: [roleGuard([Role.OrganizationOwner, Role.PlatformAdmin])],
        data: { breadcrumb: 'Organization' },
        loadComponent: () =>
          import('./features/organization/organization-settings.component')
            .then(m => m.OrganizationSettingsComponent)
      },
      {
        path: 'forbidden',
        data: { breadcrumb: 'Access denied' },
        loadComponent: () =>
          import('./features/errors/forbidden.component').then(m => m.ForbiddenComponent)
      },
      {
        path: '**',
        loadComponent: () =>
          import('./features/errors/not-found.component').then(m => m.NotFoundComponent)
      }
    ]
  }
];
