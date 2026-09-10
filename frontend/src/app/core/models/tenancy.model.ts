export enum TenancyStatus {
  UPCOMING = 'UPCOMING',
  ACTIVE = 'ACTIVE',
  PAST = 'PAST',
  EVICTED = 'EVICTED'
}

/** Statuses a tenancy can be created or edited into, in lifecycle order. */
export const TENANCY_STATUSES: TenancyStatus[] = [
  TenancyStatus.UPCOMING,
  TenancyStatus.ACTIVE,
  TenancyStatus.PAST,
  TenancyStatus.EVICTED
];

/** Statuses that close a tenancy out and release its unit. */
export const TERMINAL_TENANCY_STATUSES: TenancyStatus[] = [
  TenancyStatus.PAST,
  TenancyStatus.EVICTED
];

export function isTenancyOpen(status: TenancyStatus): boolean {
  return !TERMINAL_TENANCY_STATUSES.includes(status);
}

export interface Tenancy {
  id: string;
  organizationId: string;
  tenantId: string;
  tenantName: string;
  tenantEmail?: string | null;
  unitId: string;
  unitNumber: string;
  propertyId: string | null;
  startDate: string;
  endDate?: string | null;
  status: TenancyStatus;
  rentAmount?: number | null;
  securityDepositAmount?: number | null;
  tenantLoginPassword?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TenancyRequest {
  tenantId: string;
  unitId: string;
  startDate: string;
  endDate?: string | null;
  status: TenancyStatus;
  /** Omit to inherit the unit's rent. */
  rentAmount?: number | null;
  /** Omit to inherit the unit's deposit. */
  securityDepositAmount?: number | null;
}

export interface EndTenancyRequest {
  endDate: string;
  /** Defaults to PAST server-side. */
  status?: TenancyStatus;
}

export interface TenancyFilters {
  organizationId?: string;
  status?: TenancyStatus | null;
  tenantId?: string | null;
  unitId?: string | null;
}
