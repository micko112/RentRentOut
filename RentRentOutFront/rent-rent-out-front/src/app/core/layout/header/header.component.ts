import { Component, ElementRef, EventEmitter, HostListener, OnInit, Output } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { User } from '../../../shared/models/user.model';
import { AuthService } from '../../../features/auth/services/auth.service';
import { AdService } from '../../../features/ads/services/ad.service';
import { MobileFilterService } from '../../services/mobile-filter.service';
import { AdTemplate, AdTemplateService } from '../../../features/ads/services/ad-template.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  searchTerm: string = '';
  currentUser$!: Observable<User | null>;
  templates$!: Observable<AdTemplate[]>;
  templatesMenuOpen = false;

  @Output() openMobileMenuEvent = new EventEmitter<void>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private adService: AdService,
    private mobileFilterService: MobileFilterService,
    private adTemplateService: AdTemplateService,
    private toastService: ToastService,
    private elementRef: ElementRef,
  ) {}

  ngOnInit() {
    this.currentUser$ = this.authService.currentUser$;
    this.templates$ = this.adTemplateService.list$;

    this.currentUser$.subscribe(u => {
      if (u) this.adTemplateService.ensureLoaded();
      else this.adTemplateService.clear();
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.templatesMenuOpen) return;
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.templatesMenuOpen = false;
    }
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.templatesMenuOpen = false;
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

  toggleTemplatesMenu(event: MouseEvent) {
    event.stopPropagation();
    this.templatesMenuOpen = !this.templatesMenuOpen;
    if (this.templatesMenuOpen) this.adTemplateService.refresh();
  }

  useTemplate(t: AdTemplate) {
    this.templatesMenuOpen = false;
    this.router.navigate(['/ads/create'], { queryParams: { template: t.id } });
  }

  deleteTemplate(event: MouseEvent, t: AdTemplate) {
    event.stopPropagation();
    if (!confirm(`Obrisati šablon "${t.name}"?`)) return;
    this.adTemplateService.delete(t.id).subscribe({
      next: () => this.toastService.showSuccess('Šablon obrisan.'),
      error: () => this.toastService.showError('Greška pri brisanju šablona.'),
    });
  }

  login()      { this.router.navigate(['/login']); }
  logout()     { this.authService.logout(); }
  register()   { this.router.navigate(['/register']); }
  createAd()   { this.router.navigate(['/ads/create']); }
  myProfile()  { this.router.navigate(['/user/me']); }
  adminPanel() { this.router.navigate(['/admin']); }
}
