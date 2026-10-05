import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from './auth.service';

export interface SalesInquiry {
  id: string;
  name: string;
  email: string;
  message: string;
  createdAt: string;
}

export interface SalesInquiryRequest {
  name: string;
  email: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class SalesInquiryService {
  private readonly apiUrl = '/api/v1/sales-inquiries';

  constructor(private readonly http: HttpClient) {}

  submit(request: SalesInquiryRequest): Observable<ApiResponse<SalesInquiry>> {
    return this.http.post<ApiResponse<SalesInquiry>>(this.apiUrl, request);
  }

  list(): Observable<ApiResponse<{ content: SalesInquiry[] }>> {
    return this.http.get<ApiResponse<{ content: SalesInquiry[] }>>(`${this.apiUrl}?page=0&size=100`);
  }
}