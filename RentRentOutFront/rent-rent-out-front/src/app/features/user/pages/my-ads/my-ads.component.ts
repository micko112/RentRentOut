import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Subscription} from 'rxjs';
import {AdService} from '../../../ads/services/ad.service';
import {Router} from '@angular/router';
import {AdPreview} from '../../../../shared/models/adPreview.model';
import {AdCardComponent} from '../../../ads/components/ad-card/ad-card.component';
import {ToastService} from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-my-ads',
  imports: [
    CommonModule,
    AdCardComponent,
    FormsModule
  ],
  templateUrl: './my-ads.component.html',
  styleUrl: './my-ads.component.css'
})
export class MyAdsComponent implements OnInit, OnDestroy {
  currentAds: AdPreview[] = [];
  totalElements = 0;
  private adsSub?: Subscription;

  // Search
  searchQuery = '';

  // Delete modal
  showDeleteModal = false;
  adToDelete: AdPreview | null = null;
  deleteReason = '';

  deleteReasons = [
    'Više ne izdajem ovaj predmet',
    'Pronašao/la sam zakupca na drugom mestu',
    'Oglas sadrži grešku',
    'Ostalo'
  ];

  constructor(
    private adService: AdService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadAds();
  }

  ngOnDestroy() {
    this.adsSub?.unsubscribe();
  }

  private loadAds() {
    this.adsSub?.unsubscribe();
    this.adsSub = this.adService.getMyAds().subscribe({
      next: (page) => {
        this.currentAds = page.content;
        this.totalElements = page.totalElements;
      },
      error: () => this.toastService.showError('Greška pri učitavanju oglasa.')
    });
  }

  get filteredAds(): AdPreview[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.currentAds;
    return this.currentAds.filter(ad => ad.title.toLowerCase().includes(q));
  }

  openDeleteModal(ad: AdPreview) {
    this.adToDelete = ad;
    this.deleteReason = '';
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.adToDelete = null;
    this.deleteReason = '';
  }

  confirmDelete() {
    if (!this.adToDelete || !this.deleteReason) return;
    const id = this.adToDelete.id;
    this.closeDeleteModal();
    this.adService.deleteAd(id).subscribe({
      next: () => {
        this.toastService.showSuccess('Uspesno ste obrisali oglas!');
        this.loadAds();
      },
      error: () => this.toastService.showError('Greška pri brisanju oglasa.')
    });
  }

  onEdit(adId: number) {
    this.router.navigate(['/ads/edit', adId]);
  }
}
