import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';

export interface OwnerInvoice {
  id: string;
  tenancyId?: string;
  tenantId: string;
  tenantName?: string;
  unitId: string;
  unitNumber?: string;
  periodStart: string;
  periodEnd: string;
  dueDate: string;
  amount: number;
  status: string;
  notes?: string;
}

export interface CreateInvoiceRequest {
  tenancyId?: string | null;
  tenantId: string;
  unitId: string;
  periodStart: string;
  periodEnd: string;
  dueDate: string;
  amount?: number | null;
  notes?: string | null;
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly apiUrl = '/api/v1/invoices';

  constructor(private http: HttpClient) {}

  getInvoices(page = 0, size = 20): Observable<ApiResponse<Page<OwnerInvoice>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<Page<OwnerInvoice>>>(this.apiUrl, { params });
  }

  createInvoice(request: CreateInvoiceRequest): Observable<ApiResponse<OwnerInvoice>> {
    return this.http.post<ApiResponse<OwnerInvoice>>(this.apiUrl, request);
  }

  issueInvoice(id: string): Observable<ApiResponse<OwnerInvoice>> {
    return this.http.post<ApiResponse<OwnerInvoice>>(`${this.apiUrl}/${id}/issue`, {});
  }
}
