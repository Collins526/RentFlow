/**
 * Role names exactly as the backend stores and serialises them. Spring adds the
 * `ROLE_` prefix internally for `hasRole(...)`, but the token payload carries the
 * bare names, so the frontend compares against these.
 */
export const Role = {
  PlatformAdmin: 'PLATFORM_ADMIN',
  OrganizationOwner: 'ORGANIZATION_OWNER',
  PropertyManager: 'PROPERTY_MANAGER',
  Accountant: 'ACCOUNTANT',
  Tenant: 'TENANT'
} as const;

export type RoleName = (typeof Role)[keyof typeof Role];

/**
 * Mirrors the `READ_ROLES` constant shared by the property, unit, tenant, tenancy
 * and dashboard controllers.
 */
export const PORTFOLIO_READ_ROLES: RoleName[] = [
  Role.PlatformAdmin,
  Role.OrganizationOwner,
  Role.PropertyManager,
  Role.Accountant
];

/** Mirrors the controllers' `MANAGE_ROLES`. */
export const PORTFOLIO_MANAGE_ROLES: RoleName[] = [
  Role.PlatformAdmin,
  Role.OrganizationOwner,
  Role.PropertyManager
];
