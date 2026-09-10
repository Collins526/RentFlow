import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse } from '../auth.service';

export interface Organization {
  id: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  logoUrl: string;
  status: string;
}

export interface OrganizationUpdateRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  logoUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private apiUrl = '/api/v1/organizations';
  
  currentOrganization = signal<Organization | null>(null);

  constructor(private http: HttpClient) {}

  getMyOrganization(): Observable<ApiResponse<Organization>> {
    return this.http.get<ApiResponse<Organization>>(`${this.apiUrl}/my-organization`).pipe(
      tap(response => {
        if (response.success) {
          this.currentOrganization.set(response.data);
        }
      })
    );
  }

  updateMyOrganization(data: OrganizationUpdateRequest): Observable<ApiResponse<Organization>> {
    return this.http.put<ApiResponse<Organization>>(`${this.apiUrl}/my-organization`, data).pipe(
      tap(response => {
        if (response.success) {
          this.currentOrganization.set(response.data);
        }
      })
    );
  }

  // Platform admin endpoints
  getAllOrganizations(page = 0, size = 100): Observable<ApiResponse<{ content: Organization[] }>> {
    return this.http.get<ApiResponse<{ content: Organization[] }>>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getOrganizationById(id: string): Observable<ApiResponse<Organization>> {
    return this.http.get<ApiResponse<Organization>>(`${this.apiUrl}/${id}`).pipe(
      tap(response => {
        if (response.success) {
          this.currentOrganization.set(response.data);
        }
      })
    );
  }

  updateOrganizationById(id: string, data: OrganizationUpdateRequest): Observable<ApiResponse<Organization>> {
    return this.http.put<ApiResponse<Organization>>(`${this.apiUrl}/${id}`, data).pipe(
      tap(response => {
        if (response.success) {
          this.currentOrganization.set(response.data);
        }
      })
    );
  }

  deleteOrganizationById(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  suspendOrganization(id: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/suspend`, {});
  }

  activateOrganization(id: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/activate`, {});
  }
}
