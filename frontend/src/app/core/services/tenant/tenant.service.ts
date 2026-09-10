import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';
import { Tenant, TenantRequest } from '../../models/tenant.model';

@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private apiUrl = '/api/v1/tenants';

  constructor(private http: HttpClient) {}

  getAllTenants(page: number = 0, size: number = 10, organizationId?: string): Observable<ApiResponse<Page<Tenant>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (organizationId) {
      params = params.set('organizationId', organizationId);
    }
    return this.http.get<ApiResponse<Page<Tenant>>>(this.apiUrl, { params });
  }

  getTenantById(id: string): Observable<ApiResponse<Tenant>> {
    return this.http.get<ApiResponse<Tenant>>(`${this.apiUrl}/${id}`);
  }

  createTenant(data: TenantRequest): Observable<ApiResponse<Tenant>> {
    return this.http.post<ApiResponse<Tenant>>(this.apiUrl, data);
  }

  updateTenant(id: string, data: TenantRequest): Observable<ApiResponse<Tenant>> {
    return this.http.put<ApiResponse<Tenant>>(`${this.apiUrl}/${id}`, data);
  }

  deleteTenant(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
