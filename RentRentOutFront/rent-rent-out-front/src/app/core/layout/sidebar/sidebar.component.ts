import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';
import { NotificationService } from '../../services/notification.service';

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
  currentUser$ = this.authService.currentUser$;
  totalUnread$ = this.notificationService.totalUnread$;

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.notificationService.initialize();
    }
  }
}
