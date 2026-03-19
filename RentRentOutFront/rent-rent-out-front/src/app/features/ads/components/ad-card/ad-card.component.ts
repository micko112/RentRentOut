import {Component, Input, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterModule} from '@angular/router';
import {AdPreview} from '../../../../shared/models/adPreview.model';
import {AdService} from '../../services/ad.service';

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

  constructor(private adService: AdService) {}

  ngOnInit(): void {
    this.isLoggedIn = !!localStorage.getItem('authToken');
    this.isSaved = this.ad.saved ?? false;
  }

  get imageUrl(): string {
    if (this.ad.thumbnail) {
      return this.ad.thumbnail;
    }
    return 'assets/images/placeholder.png';
  }

  handleImageError(event: any) {
    event.target.src = 'assets/images/placeholder.png';
    event.target.onerror = null;
  }

  intervalMap: {[key: string]: string} = {
    'PER_DAY': '/dan',
    'PER_HOUR': '/sat',
    'PER_MONTH': '/mesečno'
  };

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
