import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './core/layout/navbar/navbar.component';
import { FooterComponent } from './core/layout/footer/footer.component';
import { HeaderComponent } from './core/layout/header/header.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { CookieBannerComponent } from './shared/components/cookie-banner/cookie-banner.component';
import { SupportWidgetComponent } from './features/support/support-widget/support-widget.component';
import { NotificationService } from './core/services/notification.service';
import { SidebarStateService } from './core/services/sidebar-state.service';
import { MobileFilterService } from './core/services/mobile-filter.service';
import { CapacitorAppService } from './core/services/capacitor-app.service';
import { map, filter, startWith } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavbarComponent, FooterComponent, HeaderComponent, ToastComponent, SidebarComponent, CookieBannerComponent, SupportWidgetComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'rent-rent-out-front';

  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private sidebarState = inject(SidebarStateService);
  private mobileFilterService = inject(MobileFilterService);
  private capacitorApp = inject(CapacitorAppService);

  ngOnInit(): void {
    this.capacitorApp.initialize();
  }

  sidebarCollapsed$ = this.sidebarState.collapsed$;
  chatUnread$ = this.notificationService.totalUnread$;
  mobileMenuOpen = false;

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

  showMobileSearch$ = this.route$.pipe(
    map(url => {
      if (url.startsWith('/admin')) return false;
      if (this.NO_SIDEBAR_ROUTES.some(r => url.startsWith(r))) return false;
      if (url.startsWith('/messages')) return false;
      return true;
    })
  );

  isAdmin$ = this.route$.pipe(
    map(url => url.startsWith('/admin'))
  );

  showSupportWidget$ = this.route$.pipe(
    map(url => !url.startsWith('/admin') && !url.startsWith('/messages'))
  );

  onMobileSearch(term: string): void {
    if (term.trim()) {
      this.router.navigate(['/ads'], { queryParams: { keyword: term.trim() } });
    }
  }

  triggerMobileFilter(): void {
    this.mobileFilterService.toggle();
  }
}
