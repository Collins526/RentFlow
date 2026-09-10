import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';

export interface Property {
  id: string;
  organizationId: string;
  name: string;
  type: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  description: string;
  status: string;
  units: number;
  numberOfUnits: number;
  numberOfFloors: number;
}

export interface PropertyRequest {
  name: string;
  type: string;
  address: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  description?: string;
  status?: string;
  numberOfUnits?: number;
  numberOfFloors?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class PropertyService {
  private apiUrl = '/api/v1/properties';

  constructor(private http: HttpClient) {}

  getAllProperties(page: number = 0, size: number = 10, organizationId?: string): Observable<ApiResponse<Page<Property>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (organizationId) {
      params = params.set('organizationId', organizationId);
    }
    return this.http.get<ApiResponse<Page<Property>>>(this.apiUrl, { params });
  }

  getPropertyById(id: string): Observable<ApiResponse<Property>> {
    return this.http.get<ApiResponse<Property>>(`${this.apiUrl}/${id}`);
  }

  createProperty(data: PropertyRequest): Observable<ApiResponse<Property>> {
    return this.http.post<ApiResponse<Property>>(this.apiUrl, data);
  }

  updateProperty(id: string, data: PropertyRequest): Observable<ApiResponse<Property>> {
    return this.http.put<ApiResponse<Property>>(`${this.apiUrl}/${id}`, data);
  }

  deleteProperty(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
