import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {AdService} from '../../../ads/services/ad.service';
import {AdCardComponent} from '../../../ads/components/ad-card/ad-card.component';
import {ToastService} from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-saved-ads',
  standalone: true,
  imports: [CommonModule, RouterLink, AdCardComponent],
  templateUrl: './saved-ads.component.html',
  styleUrl: './saved-ads.component.css'
})
export class SavedAdsComponent implements OnInit {
  adsPage: Page<AdPreview> | null = null;
  isLoading: boolean = true;
  currentPage: number = 0;
  pageSize: number = 12;

  constructor(private adService: AdService, private toastService: ToastService) {}

  ngOnInit(): void {
    this.loadSavedAds(0);
  }

  loadSavedAds(page: number): void {
    this.isLoading = true;
    this.adService.getSavedAds(page, this.pageSize).subscribe({
      next: (result) => {
        this.adsPage = result;
        this.currentPage = page;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.toastService.showError('Greška pri učitavanju sačuvanih oglasa.');
      }
    });
  }

  goToPage(pageIndex: number): void {
    this.loadSavedAds(pageIndex);
  }
}
