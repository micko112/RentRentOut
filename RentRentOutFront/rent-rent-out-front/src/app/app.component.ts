import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NavbarComponent} from './core/layout/navbar/navbar.component';
import {HeaderComponent} from './core/layout/header/header.component';
import {ToastComponent} from './shared/components/toast/toast.component';
import {SidebarComponent} from './shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, HeaderComponent, SidebarComponent, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'rent-rent-out-front';

  private authService = inject(AuthService);
  isLoggedIn$ = this.authService.currentUser$.pipe(map(user => !!user));
}
