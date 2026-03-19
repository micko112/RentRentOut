import { Component, inject } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule, AsyncPipe } from '@angular/common';
import { NavbarComponent } from './core/layout/navbar/navbar.component';
import { FooterComponent } from './core/layout/footer/footer.component';
import { HeaderComponent } from './core/layout/header/header.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { AuthService } from './features/auth/services/auth.service';
import { combineLatest, map, filter, startWith } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, AsyncPipe, NavbarComponent, FooterComponent, HeaderComponent, ToastComponent, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'rent-rent-out-front';

  private router = inject(Router);
  private authService = inject(AuthService);

  private route$ = this.router.events.pipe(
    filter(e => e instanceof NavigationEnd),
    map((e) => (e as NavigationEnd).urlAfterRedirects),
    startWith(this.router.url)
  );

  showSidebar$ = combineLatest([
    this.authService.currentUser$,
    this.route$
  ]).pipe(
    map(([user, url]) => !!user && !url.startsWith('/admin'))
  );

  isAdmin$ = this.route$.pipe(
    map(url => url.startsWith('/admin'))
  );
}
