import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';

export interface Block {
  id: string;
  propertyId: string;
  name: string;
  description: string;
  numberOfFloors: number;
}

export interface BlockRequest {
  name: string;
  description?: string;
  numberOfFloors?: number;
}

@Injectable({
  providedIn: 'root'
})
export class BlockService {
  constructor(private http: HttpClient) {}

  getBlocksByPropertyId(propertyId: string, page: number = 0, size: number = 10): Observable<ApiResponse<Page<Block>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<Page<Block>>>(`/api/v1/properties/${propertyId}/blocks`, { params });
  }

  getBlockById(id: string): Observable<ApiResponse<Block>> {
    return this.http.get<ApiResponse<Block>>(`/api/v1/blocks/${id}`);
  }

  createBlock(propertyId: string, data: BlockRequest): Observable<ApiResponse<Block>> {
    return this.http.post<ApiResponse<Block>>(`/api/v1/properties/${propertyId}/blocks`, data);
  }

  updateBlock(id: string, data: BlockRequest): Observable<ApiResponse<Block>> {
    return this.http.put<ApiResponse<Block>>(`/api/v1/blocks/${id}`, data);
  }

  deleteBlock(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`/api/v1/blocks/${id}`);
  }
}
