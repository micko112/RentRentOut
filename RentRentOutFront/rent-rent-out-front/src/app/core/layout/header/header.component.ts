import { Component, EventEmitter, Output } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { User } from '../../../shared/models/user.model';
import { AuthService } from '../../../features/auth/services/auth.service';
import { AdService } from '../../../features/ads/services/ad.service';
import { MobileFilterService } from '../../services/mobile-filter.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  searchTerm: string = '';
  currentUser$!: Observable<User | null>;

  @Output() openMobileMenuEvent = new EventEmitter<void>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private adService: AdService,
    private mobileFilterService: MobileFilterService
  ) {}

  ngOnInit() {
    this.currentUser$ = this.authService.currentUser$;
  }

  onSearch() {
    this.router.navigate(['/ads'], { queryParams: { keyword: this.searchTerm } });
  }

  triggerMobileFilter() {
    if (this.router.url.startsWith('/ads') || this.router.url === '/') {
      this.mobileFilterService.toggle();
    } else {
      this.router.navigate(['/ads']);
    }
  }

  navigateSaved() {
    this.router.navigate(['/user/saved-ads']);
  }

  login()      { this.router.navigate(['/login']); }
  logout()     { this.authService.logout(); }
  register()   { this.router.navigate(['/register']); }
  createAd()   { this.router.navigate(['/ads/create']); }
  myProfile()  { this.router.navigate(['/user/me']); }
  adminPanel() { this.router.navigate(['/admin']); }
}
