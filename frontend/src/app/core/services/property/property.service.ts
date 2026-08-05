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

  getAllProperties(page: number = 0, size: number = 10): Observable<ApiResponse<Page<Property>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
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
