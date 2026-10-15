import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse } from '../auth.service';
import { Page } from '../property/property.service';
import { ToastService } from '../toast.service';

export interface TenantAnnouncement {
  id: string;
  title: string;
  message: string;
  createdAt?: string;
}

export interface CreateTenantAnnouncement {
  title: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly apiUrl = '/api/v1/notifications';
  readonly unreadCount = signal<number | null>(null);

  constructor(private http: HttpClient, private toast: ToastService) {}

  announceToAllTenants(request: CreateTenantAnnouncement): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/announcements`, request);
  }

  getMyAnnouncements(page = 0, size = 20): Observable<ApiResponse<Page<TenantAnnouncement>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<ApiResponse<Page<TenantAnnouncement>>>(`${this.apiUrl}/my/announcements`, { params });
  }

  refreshUnreadCount(): void {
    this.http.get<ApiResponse<number>>(`${this.apiUrl}/my/unread-count`).subscribe({
      next: response => {
        if (!response.success) {
          this.unreadCount.set(null);
          this.toast.error(response.message || 'Could not load notification count.');
          return;
        }
        this.unreadCount.set(response.data);
      },
      error: error => {
        this.unreadCount.set(null);
        this.toast.apiError(error, 'Could not load notification count.');
      }
    });
  }

  markMyAnnouncementsRead(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/my/announcements/read`, {}).pipe(
      tap(response => {
        if (response.success) {
          this.refreshUnreadCount();
        }
      })
    );
  }
}
