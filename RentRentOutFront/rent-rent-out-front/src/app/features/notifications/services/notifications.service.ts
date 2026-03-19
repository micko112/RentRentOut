import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AppNotification } from '../../../shared/models/notification.model';
import { API_BASE_URL } from '../../../core/config/api.config';

@Injectable({ providedIn: 'root' })
export class NotificationsService {

  private readonly url = `${API_BASE_URL}/notifications`;

  private unreadCountSubject = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadUnreadCount(): void {
    this.http.get<{ count: number }>(`${this.url}/unread-count`).subscribe({
      next: res => this.unreadCountSubject.next(res.count),
      error: () => {}
    });
  }

  getAll(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.url);
  }

  markOneAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/read`, {}).pipe(
      tap(() => {
        const current = this.unreadCountSubject.value;
        if (current > 0) this.unreadCountSubject.next(current - 1);
      })
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.url}/read-all`, {}).pipe(
      tap(() => this.unreadCountSubject.next(0))
    );
  }
}
