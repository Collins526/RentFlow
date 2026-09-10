import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';

export interface DashboardSummary {
  totalOrganizations: number;
  organizations: DashboardOrganizationSummary[];
  totalProperties: number;
  totalBlocks: number;
  totalUnits: number;
  occupiedUnits: number;
  vacantUnits: number;
  reservedUnits: number;
  unitsUnderMaintenance: number;
  occupancyRate: number;
  totalTenants: number;
  activeTenancies: number;
  upcomingTenancies: number;
  contractedMonthlyRent: number;
  potentialMonthlyRent: number;
}

export interface DashboardOrganizationSummary {
  id: string;
  name: string;
  email?: string | null;
  properties: number;
  units: number;
  tenants: number;
  activeTenancies: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  getSummary(): Observable<ApiResponse<DashboardSummary>> {
    return this.http.get<ApiResponse<DashboardSummary>>('/api/v1/dashboard/summary');
  }
}
