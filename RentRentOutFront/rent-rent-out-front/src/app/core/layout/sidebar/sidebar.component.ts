import { Component, EventEmitter, HostBinding, inject, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../features/auth/services/auth.service';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';
import { NotificationService } from '../../services/notification.service';
import { NotificationsService } from '../../../features/notifications/services/notifications.service';
import { PushNotificationService } from '../../services/push-notification.service';
import { SidebarStateService } from '../../services/sidebar-state.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslateModule, LanguageSwitcherComponent],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private notificationsService = inject(NotificationsService);
  private pushNotificationService = inject(PushNotificationService);
  private sidebarState = inject(SidebarStateService);
  private router = inject(Router);

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

  logout(): void {
    this.authService.logout();
    this.closeDrawer.emit();
  }

  goToLogin(): void {
    this.closeDrawer.emit();
    this.router.navigate(['/login']);
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
