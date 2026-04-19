import { Component, EventEmitter, HostBinding, inject, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationsService } from '../../../features/notifications/services/notifications.service';
import { PushNotificationService } from '../../services/push-notification.service';
import { SidebarStateService } from '../../services/sidebar-state.service';

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
  private sidebarState = inject(SidebarStateService);

  currentUser$ = this.authService.currentUser$;
  totalUnread$ = this.notificationService.totalUnread$;
  notifUnread$ = this.notificationsService.unreadCount$;
  isCollapsed$ = this.sidebarState.collapsed$;

  @Input() mobileDrawer = false;
  @Output() closeDrawer = new EventEmitter<void>();

  @HostBinding('class.mobile-drawer-mode') get isMobileDrawer() {
    return this.mobileDrawer;
  }

  toggle(): void {
    this.sidebarState.toggle();
  }

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.notificationService.initialize();
      this.notificationsService.loadUnreadCount();
      this.pushNotificationService.requestAndSubscribe();
    }
  }
}
