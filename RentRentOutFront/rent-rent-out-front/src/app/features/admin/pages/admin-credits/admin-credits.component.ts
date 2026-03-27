import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { AdminService, UserCreditSummary } from '../../services/admin.service';
import { PromotionService } from '../../../ads/services/promotion.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { Page } from '../../../../shared/models/adPreview.model';

@Component({
  selector: 'app-admin-credits',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-credits.component.html',
  styleUrl: './admin-credits.component.css'
})
export class AdminCreditsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  creditsPage: Page<UserCreditSummary> | null = null;
  loading = true;
  currentPage = 0;
  searchQuery = '';

  // Credit modal
  showCreditModal = false;
  creditUser: UserCreditSummary | null = null;
  creditAmount: number | null = null;
  creditDescription = '';
  addingCredit = false;

  readonly skeletonRows = Array(8);

  constructor(
    private adminService: AdminService,
    private promotionService: PromotionService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.load();
    });

    this.load();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.loading = true;
    this.adminService.getUserCreditSummaries(this.currentPage, 20, this.searchQuery)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.creditsPage = page;
          this.loading = false;
        },
        error: () => {
          this.toastService.showError('Greška pri učitavanju podataka o kreditima.');
          this.loading = false;
        }
      });
  }

  onSearchChange() {
    this.searchSubject.next(this.searchQuery);
  }

  prevPage() {
    if (this.creditsPage && !this.creditsPage.first) {
      this.currentPage--;
      this.load();
    }
  }

  nextPage() {
    if (this.creditsPage && !this.creditsPage.last) {
      this.currentPage++;
      this.load();
    }
  }

  openCreditModal(user: UserCreditSummary) {
    this.creditUser = user;
    this.creditAmount = null;
    this.creditDescription = '';
    this.showCreditModal = true;
  }

  closeCreditModal() {
    this.showCreditModal = false;
    this.creditUser = null;
  }

  confirmAddCredit() {
    if (!this.creditUser || !this.creditAmount || this.creditAmount <= 0 || this.addingCredit) return;
    this.addingCredit = true;
    this.promotionService.addCredit(this.creditUser.userId, this.creditAmount, this.creditDescription)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(`Dodato ${this.creditAmount} RSD korisniku ${this.creditUser!.fullName}.`);
          this.addingCredit = false;
          this.closeCreditModal();
          this.load();
        },
        error: () => {
          this.toastService.showError('Greška pri dodavanju kredita.');
          this.addingCredit = false;
        }
      });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return 'Danas';
    if (diffDays === 1) return 'Juče';
    if (diffDays < 7) return `Pre ${diffDays} dana`;
    if (diffDays < 30) return `Pre ${Math.floor(diffDays / 7)} ned.`;
    return d.toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit', year: '2-digit' });
  }
}
