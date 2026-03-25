import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { AdPreview, Page } from '../../../../shared/models/adPreview.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-ads',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-ads.component.html',
  styleUrl: './admin-ads.component.css'
})
export class AdminAdsComponent implements OnInit {
  adsPage: Page<AdPreview> | null = null;
  loading = true;
  currentPage = 0;

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadAds();
  }

  loadAds() {
    this.loading = true;
    this.adminService.getAds(this.currentPage).subscribe({
      next: (page) => {
        this.adsPage = page;
        this.loading = false;
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju oglasa.');
        this.loading = false;
      }
    });
  }

  suspendAd(ad: AdPreview) {
    this.adminService.suspendAd(ad.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Oglas je suspendovan.');
        this.loadAds();
      },
      error: () => this.toastService.showError('Greška pri suspenziji oglasa.')
    });
  }

  unsuspendAd(ad: AdPreview) {
    this.adminService.unsuspendAd(ad.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Oglas je reaktiviran.');
        this.loadAds();
      },
      error: () => this.toastService.showError('Greška pri reaktivaciji oglasa.')
    });
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadAds();
    }
  }

  nextPage() {
    if (this.adsPage && !this.adsPage.last) {
      this.currentPage++;
      this.loadAds();
    }
  }
}
