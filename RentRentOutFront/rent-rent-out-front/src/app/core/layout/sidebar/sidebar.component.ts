import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationsService } from '../../../features/notifications/services/notifications.service';
import { PushNotificationService } from '../../services/push-notification.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private notificationsService = inject(NotificationsService);
  private pushNotificationService = inject(PushNotificationService);
  currentUser$ = this.authService.currentUser$;
  totalUnread$ = this.notificationService.totalUnread$;
  notifUnread$ = this.notificationsService.unreadCount$;

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.notificationService.initialize();
      this.notificationsService.loadUnreadCount();
      this.pushNotificationService.requestAndSubscribe();
    }
  }
}
