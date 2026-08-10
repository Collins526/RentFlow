export enum TenantType {
  INDIVIDUAL = 'INDIVIDUAL',
  CORPORATE = 'CORPORATE'
}

export interface Tenant {
  id: string;
  organizationId: string;
  tenantType: TenantType;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phoneNumber: string;
  identificationType?: string;
  identificationNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface TenantRequest {
  tenantType: TenantType;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phoneNumber: string;
  identificationType?: string;
  identificationNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  status?: string;
}
