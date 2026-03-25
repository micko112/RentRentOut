import {Component, Input, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterModule} from '@angular/router';
import {AdPreview} from '../../../../shared/models/adPreview.model';
import {AdService} from '../../services/ad.service';
import {AuthService} from '../../../auth/services/auth.service';

@Component({
  selector: 'app-ad-card',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterModule],
  templateUrl: './ad-card.component.html',
  styleUrl: './ad-card.component.css'
})
export class AdCardComponent implements OnInit {
  @Input() ad!: AdPreview;
  @Input() viewMode: 'grid' | 'list' = 'grid';

  isSaved: boolean = false;
  isSavingToggle: boolean = false;
  isLoggedIn: boolean = false;

  constructor(private adService: AdService, private authService: AuthService) {}

  ngOnInit(): void {
    this.isLoggedIn = !!this.authService.currentUserValue;
    this.isSaved = this.ad.saved ?? false;
  }

  get imageUrl(): string {
    if (this.ad.thumbnail) {
      return this.ad.thumbnail;
    }
    return 'assets/images/placeholder.png';
  }

  handleImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/placeholder.png';
    img.onerror = null;
  }

  intervalMap: {[key: string]: string} = {
    'PER_DAY': '/dan',
    'PER_HOUR': '/sat',
    'PER_MONTH': '/mesečno'
  };

  get priceRange(): string {
    const base = this.ad.price;
    const sym = this.ad.currency === 'EUR' ? '€' : 'din';
    const interval = this.intervalMap[this.ad.priceInterval] || '';

    // Find cheapest tier (lowest price = best deal for renter)
    const tiers = [
      this.ad.pricePerWeek,
      this.ad.pricePerMonth,
    ].filter((v): v is number => v != null && v > 0);

    if (tiers.length === 0) return '';

    const minTier = Math.min(...tiers);

    // Only show range if cheapest tier is actually cheaper
    if (minTier >= base) return '';

    return `${Math.round(minTier).toLocaleString('sr-RS')} – ${Math.round(base).toLocaleString('sr-RS')} ${sym}${interval}`;
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMinutes = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    const diffMonths = Math.floor(diffDays / 30);
    const diffYears = Math.floor(diffDays / 365);

    if (diffMinutes < 60) return 'Danas';

    if (diffHours < 24) {
      if (diffHours === 1) return 'Pre 1 sat';
      if (diffHours <= 4) return `Pre ${diffHours} sata`;
      return `Pre ${diffHours} sati`;
    }

    if (diffDays < 30) {
      if (diffDays === 1) return 'Pre 1 dan';
      if (diffDays <= 4) return `Pre ${diffDays} dana`;
      return `Pre ${diffDays} dana`;
    }

    if (diffMonths < 12) {
      if (diffMonths === 1) return 'Pre 1 mesec';
      if (diffMonths <= 4) return `Pre ${diffMonths} meseca`;
      return `Pre ${diffMonths} meseci`;
    }

    if (diffYears === 1) return 'Pre 1 godinu';
    if (diffYears <= 4) return `Pre ${diffYears} godine`;
    return `Pre ${diffYears} godina`;
  }

  toggleSave(event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();

    if (this.isSavingToggle) return;
    this.isSavingToggle = true;

    const wasSaved = this.isSaved;
    this.isSaved = !wasSaved;

    const request$ = wasSaved
      ? this.adService.unsaveAd(this.ad.id)
      : this.adService.saveAd(this.ad.id);

    request$.subscribe({
      next: () => {
        this.isSavingToggle = false;
      },
      error: () => {
        this.isSaved = wasSaved;
        this.isSavingToggle = false;
      }
    });
  }
}
