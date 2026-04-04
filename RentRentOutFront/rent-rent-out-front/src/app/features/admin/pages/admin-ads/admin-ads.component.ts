import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { AdminService } from '../../services/admin.service';
import { AdPreview, Page } from '../../../../shared/models/adPreview.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-ads',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-ads.component.html',
  styleUrl: './admin-ads.component.css'
})
export class AdminAdsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  adsPage: Page<AdPreview> | null = null;
  loading = true;
  currentPage = 0;
  searchQuery = '';
  statusFilter = '';

  readonly STATUS_OPTIONS = [
    { value: '',                   label: 'Svi statusi' },
    { value: 'ACTIVE',             label: 'Aktivni' },
    { value: 'SUSPENDED_BY_ADMIN', label: 'Suspendovani' },
    { value: 'ARCHIVED',           label: 'Arhivirani' },
    { value: 'DRAFT',              label: 'Draft' },
  ];

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadAds();
    });
    this.loadAds();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchChange() { this.searchSubject.next(this.searchQuery); }

  onStatusChange() {
    this.currentPage = 0;
    this.loadAds();
  }

  trackByAd(_: number, ad: AdPreview) { return ad.id; }

  loadAds() {
    this.loading = true;
    this.adminService.getAds(this.currentPage, 20, this.searchQuery, this.statusFilter)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => { this.adsPage = page; this.loading = false; },
        error: () => { this.toastService.showError('Greška pri učitavanju oglasa.'); this.loading = false; }
      });
  }

  suspendAd(ad: AdPreview) {
    this.adminService.suspendAd(ad.id).subscribe({
      next: () => { this.toastService.showSuccess('Oglas suspendovan.'); this.loadAds(); },
      error: () => this.toastService.showError('Greška pri suspenziji oglasa.')
    });
  }

  unsuspendAd(ad: AdPreview) {
    this.adminService.unsuspendAd(ad.id).subscribe({
      next: () => { this.toastService.showSuccess('Oglas reaktiviran.'); this.loadAds(); },
      error: () => this.toastService.showError('Greška pri reaktivaciji oglasa.')
    });
  }

  deleteAd(ad: AdPreview) {
    if (!confirm(`Obriši oglas "${ad.title}" trajno? Ova akcija se ne može poništiti.`)) return;
    this.adminService.deleteAd(ad.id).subscribe({
      next: () => { this.toastService.showSuccess('Oglas obrisan.'); this.loadAds(); },
      error: () => this.toastService.showError('Greška pri brisanju oglasa.')
    });
  }

  prevPage() { if (this.currentPage > 0) { this.currentPage--; this.loadAds(); } }
  nextPage() { if (this.adsPage && !this.adsPage.last) { this.currentPage++; this.loadAds(); } }
}
