import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';

export interface TenantInvoice {
  id: string;
  tenantId: string;
  tenantName?: string;
  unitNumber?: string;
  unitId?: string;
  amount: number;
  status: string;
  periodStart?: string;
  periodEnd?: string;
  dueDate?: string;
  notes?: string;
}

export interface TenantPayment {
  id: string;
  tenantId: string;
  invoiceId?: string;
  externalReference?: string;
  amount: number;
  method?: string;
  status: string;
  paymentDate?: string;
  reference?: string;
}

export interface TenantMaintenanceRequest {
  id: string;
  tenantId: string;
  unitId?: string;
  title: string;
  description?: string;
  attachmentData?: string;
  attachmentName?: string;
  attachmentType?: string;
  attachmentSize?: number;
  status: string;
  priority?: string;
  requestedDate?: string;
}

export interface CreateTenantMaintenanceRequest {
  tenantId: string;
  unitId?: string | null;
  title: string;
  description?: string | null;
  attachmentData?: string | null;
  attachmentName?: string | null;
  attachmentType?: string | null;
  attachmentSize?: number | null;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'REQUESTED';
  requestedDate: string;
}

export interface CreateTenantPaymentRequest {
  tenantId: string;
  unitId?: string | null;
  invoiceId?: string | null;
  amount: number;
  method: 'MPESA' | 'CASH' | 'CARD' | 'BANK_TRANSFER' | 'MOBILE_MONEY';
  reference?: string | null;
  phoneNumber?: string | null;
  paymentDate?: string | null;
}

export interface MpesaStkPushRequest {
  phoneNumber: string;
  amount: number;
  tenantId: string;
  invoiceId?: string | null;
  unitId?: string | null;
  reference?: string | null;
  accountReference?: string | null;
  transactionDesc?: string | null;
}

@Injectable({ providedIn: 'root' })
export class TenantPortalService {
  private readonly apiUrl = '/api/v1';

  constructor(private http: HttpClient) {}

  getInvoices(tenantId: string, page = 0, size = 10): Observable<ApiResponse<Page<TenantInvoice>>> {
    const params = new HttpParams()
      .set('tenantId', tenantId)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<Page<TenantInvoice>>>(`${this.apiUrl}/invoices`, { params });
  }

  getPayments(tenantId: string, page = 0, size = 10): Observable<ApiResponse<Page<TenantPayment>>> {
    const params = new HttpParams()
      .set('tenantId', tenantId)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<ApiResponse<Page<TenantPayment>>>(`${this.apiUrl}/payments`, { params });
  }

  getMaintenanceRequests(tenantId: string, page = 0, size = 10): Observable<ApiResponse<Page<TenantMaintenanceRequest>>> {
    const params = new HttpParams()
      .set('tenantId', tenantId)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<Page<TenantMaintenanceRequest>>>(`${this.apiUrl}/maintenance`, { params });
  }

  createMaintenanceRequest(request: CreateTenantMaintenanceRequest): Observable<ApiResponse<TenantMaintenanceRequest>> {
    return this.http.post<ApiResponse<TenantMaintenanceRequest>>(`${this.apiUrl}/maintenance`, request);
  }

  updateMaintenanceRequest(requestId: string, request: CreateTenantMaintenanceRequest): Observable<ApiResponse<TenantMaintenanceRequest>> {
    return this.http.put<ApiResponse<TenantMaintenanceRequest>>(`${this.apiUrl}/maintenance/${requestId}`, request);
  }

  cancelMaintenanceRequest(requestId: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/maintenance/${requestId}/cancel`, {});
  }

  createPayment(request: CreateTenantPaymentRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/payments`, request);
  }

  deletePayment(paymentId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/payments/${paymentId}`);
  }

  initiateMpesaStkPush(request: MpesaStkPushRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/mpesa/stk-push`, request);
  }
}
