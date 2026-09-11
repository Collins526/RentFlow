import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';

export interface MaintenanceRequestItem {
  id: string;
  tenantId: string;
  tenantName?: string;
  unitId?: string;
  unitNumber?: string;
  title: string;
  description?: string;
  attachmentData?: string;
  attachmentName?: string;
  priority: string;
  status: string;
  requestedDate?: string;
  scheduledDate?: string;
  completedDate?: string;
  assignedTo?: string;
}

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private readonly apiUrl = '/api/v1/maintenance';

  constructor(private http: HttpClient) {}

  getRequests(page = 0, size = 100, status?: string): Observable<ApiResponse<Page<MaintenanceRequestItem>>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString()).set('sort', 'createdAt,desc');
    if (status) params = params.set('status', status);
    return this.http.get<ApiResponse<Page<MaintenanceRequestItem>>>(this.apiUrl, { params });
  }
}
