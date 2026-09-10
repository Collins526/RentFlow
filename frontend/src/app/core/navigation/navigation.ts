import { PORTFOLIO_READ_ROLES, Role, RoleName } from '../auth/roles';

export interface NavItem {
  label: string;
  /** Material icon ligature. */
  icon: string;
  route: string;
  /**
   * Roles allowed to see and reach the item. An empty list means any signed-in
   * user. These mirror the `@PreAuthorize` rules on the matching controller, so
   * the nav never offers a link that would come back 403.
   */
  roles: RoleName[];
  /** Platform admins drill into portfolio data from an organization instead. */
  hideFromPlatformAdmin?: boolean;
  /**
   * Match the route as a prefix rather than exactly, so `/properties/:id` keeps
   * the Properties item highlighted.
   */
  matchPrefix?: boolean;
}

export interface NavSection {
  /** Omitted for the first group, which needs no heading. */
  heading?: string;
  items: NavItem[];
}

export const NAV_SECTIONS: NavSection[] = [
  {
    items: [
      { label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: PORTFOLIO_READ_ROLES }
    ]
  },
  {
    heading: 'Portfolio',
    items: [
      {
        label: 'Properties',
        icon: 'apartment',
        route: '/properties',
        roles: PORTFOLIO_READ_ROLES,
        hideFromPlatformAdmin: true,
        matchPrefix: true
      }
    ]
  },
  {
    heading: 'Leasing',
    items: [
      {
        label: 'Tenants',
        icon: 'people',
        route: '/tenants',
        roles: PORTFOLIO_READ_ROLES,
        hideFromPlatformAdmin: true,
        matchPrefix: true
      },
      {
        label: 'Tenancies',
        icon: 'assignment_ind',
        route: '/tenancies',
        roles: PORTFOLIO_READ_ROLES,
        hideFromPlatformAdmin: true,
        matchPrefix: true
      }
    ]
  },
  {
    heading: 'Tenant',
    items: [
      {
        label: 'Tenant portal',
        icon: 'house',
        route: '/tenant',
        roles: [Role.Tenant]
      }
    ]
  },
  {
    heading: 'Administration',
    items: [
      {
        label: 'Organizations',
        icon: 'business',
        route: '/organization',
        roles: [Role.OrganizationOwner, Role.PlatformAdmin]
      }
    ]
  }
];
