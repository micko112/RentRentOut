import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NotificationsService } from '../../services/notifications.service';
import { AppNotification } from '../../../../shared/models/notification.model';

type Filter = 'all' | 'unread';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.css'
})
export class NotificationsPageComponent implements OnInit {

  notifications: AppNotification[] = [];
  isLoading = true;
  activeFilter: Filter = 'all';

  private readonly typeConfig: Record<string, { icon: string; color: string }> = {
    CONTRACT_REQUESTED: { icon: '📋', color: 'blue'   },
    CONTRACT_ACCEPTED:  { icon: '✅', color: 'green'  },
    CONTRACT_REJECTED:  { icon: '✕',  color: 'red'    },
    CONTRACT_CANCELLED: { icon: '✕',  color: 'orange' },
    CONTRACT_ACTIVE:    { icon: '🔑', color: 'purple' },
    CONTRACT_FINISHED:  { icon: '🏁', color: 'gray'   },
    NEW_REVIEW:         { icon: '⭐', color: 'yellow' },
    AD_SAVED:           { icon: 'bookmark', color: '#813181' },
  };

  constructor(private notificationsService: NotificationsService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.notificationsService.getAll().subscribe({
      next: data => { this.notifications = data; this.isLoading = false; },
      error: ()   => { this.isLoading = false; }
    });
  }

  get filtered(): AppNotification[] {
    return this.activeFilter === 'unread'
      ? this.notifications.filter(n => !n.isRead)
      : this.notifications;
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.isRead).length;
  }

  setFilter(f: Filter): void { this.activeFilter = f; }

  getIcon(type: string): string  { return this.typeConfig[type]?.icon  ?? '🔔'; }
  getColor(type: string): string { return this.typeConfig[type]?.color ?? 'gray'; }

  markAsRead(n: AppNotification): void {
    if (n.isRead) return;
    this.notificationsService.markOneAsRead(n.id).subscribe(() => { n.isRead = true; });
  }

  markAllAsRead(): void {
    this.notificationsService.markAllAsRead().subscribe(() => {
      this.notifications.forEach(n => n.isRead = true);
    });
  }

  formatTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now  = new Date();
    const diff = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diff < 60)   return 'Upravo';
    if (diff < 3600) return `Pre ${Math.floor(diff / 60)} min`;
    if (diff < 86400) return `Pre ${Math.floor(diff / 3600)} h`;

    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) return 'Juče';

    return date.toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  getRouterLink(n: AppNotification): string[] | null {
    if (n.relatedEntityType === 'CONTRACT' && n.relatedEntityId) {
      return ['/user/me/contracts'];
    }
    if (n.relatedEntityType === 'REVIEW' && n.relatedEntityId) {
      return ['/user/me/reviews'];
    }
    if (n.relatedEntityType === 'AD' && n.relatedEntityId) {
      return ['/ads', String(n.relatedEntityId)];
    }
    return null;
  }
}
