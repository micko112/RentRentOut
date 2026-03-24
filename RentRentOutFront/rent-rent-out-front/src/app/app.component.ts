import { Component, inject } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CommonModule, AsyncPipe } from '@angular/common';
import { NavbarComponent } from './core/layout/navbar/navbar.component';
import { FooterComponent } from './core/layout/footer/footer.component';
import { HeaderComponent } from './core/layout/header/header.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { NotificationService } from './core/services/notification.service';
import { map, filter, startWith } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, AsyncPipe, NavbarComponent, FooterComponent, HeaderComponent, ToastComponent, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'rent-rent-out-front';

  private router = inject(Router);
  private notificationService = inject(NotificationService);

  chatUnread$ = this.notificationService.totalUnread$;

  private route$ = this.router.events.pipe(
    filter(e => e instanceof NavigationEnd),
    map((e) => (e as NavigationEnd).urlAfterRedirects),
    startWith(this.router.url)
  );

  private readonly NO_SIDEBAR_ROUTES = ['/login', '/register', '/verify-email', '/forgot-password', '/reset-password'];

  showSidebar$ = this.route$.pipe(
    map(url => {
      if (url.startsWith('/admin')) return false;
      return !this.NO_SIDEBAR_ROUTES.some(r => url.startsWith(r));
    })
  );

  isAdmin$ = this.route$.pipe(
    map(url => url.startsWith('/admin'))
  );
}
