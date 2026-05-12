import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {AdService} from '../../../ads/services/ad.service';
import {AdCardComponent} from '../../../ads/components/ad-card/ad-card.component';
import {ToastService} from '../../../../shared/services/toast.service';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-saved-ads',
  standalone: true,
  imports: [CommonModule, RouterLink, AdCardComponent],
  templateUrl: './saved-ads.component.html',
  styleUrl: './saved-ads.component.css'
})
export class SavedAdsComponent implements OnInit, OnDestroy {
  adsPage: Page<AdPreview> | null = null;
  isLoading: boolean = true;
  currentPage: number = 0;
  pageSize: number = 12;
  private destroy$ = new Subject<void>();

  constructor(private adService: AdService, private toastService: ToastService, private router: Router) {}

  ngOnInit(): void {
    this.loadSavedAds(0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadSavedAds(page: number): void {
    this.isLoading = true;
    this.adService.getSavedAds(page, this.pageSize).pipe(takeUntil(this.destroy$)).subscribe({
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

  sendMessage(adId: number): void {
    this.router.navigate(['/messages'], { queryParams: { newChatAdId: adId } });
  }
}
