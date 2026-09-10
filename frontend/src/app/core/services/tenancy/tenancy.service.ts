import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';
import {
  EndTenancyRequest,
  Tenancy,
  TenancyFilters,
  TenancyRequest
} from '../../models/tenancy.model';

@Injectable({
  providedIn: 'root'
})
export class TenancyService {
  private apiUrl = '/api/v1/tenancies';

  constructor(private http: HttpClient) {}

  /**
   * Tenant and unit filters are mutually exclusive server-side; pass at most one.
   */
  getTenancies(
    page: number = 0,
    size: number = 10,
    filters: TenancyFilters = {}
  ): Observable<ApiResponse<Page<Tenancy>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.tenantId) {
      params = params.set('tenantId', filters.tenantId);
    }
    if (filters.unitId) {
      params = params.set('unitId', filters.unitId);
    }
    if (filters.organizationId) {
      params = params.set('organizationId', filters.organizationId);
    }

    return this.http.get<ApiResponse<Page<Tenancy>>>(this.apiUrl, { params });
  }

  getTenancyById(id: string): Observable<ApiResponse<Tenancy>> {
    return this.http.get<ApiResponse<Tenancy>>(`${this.apiUrl}/${id}`);
  }

  createTenancy(data: TenancyRequest): Observable<ApiResponse<Tenancy>> {
    return this.http.post<ApiResponse<Tenancy>>(this.apiUrl, data);
  }

  updateTenancy(id: string, data: TenancyRequest): Observable<ApiResponse<Tenancy>> {
    return this.http.put<ApiResponse<Tenancy>>(`${this.apiUrl}/${id}`, data);
  }

  /** Closes the tenancy on a given date and releases its unit. */
  endTenancy(id: string, data: EndTenancyRequest): Observable<ApiResponse<Tenancy>> {
    return this.http.patch<ApiResponse<Tenancy>>(`${this.apiUrl}/${id}/end`, data);
  }

  deleteTenancy(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
