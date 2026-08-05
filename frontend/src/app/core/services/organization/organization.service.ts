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
}
