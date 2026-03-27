import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PromotionService, AdminCreditTransaction } from '../../../ads/services/promotion.service';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-transactions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-transactions.component.html',
  styleUrl: './admin-transactions.component.css'
})
export class AdminTransactionsComponent implements OnInit {
  transactions: AdminCreditTransaction[] = [];
  loading = true;
  currentPage = 0;
  totalPages = 0;
  first = true;
  last = true;

  constructor(
    private promotionService: PromotionService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.promotionService.getAllTransactions(this.currentPage).subscribe({
      next: (page) => {
        this.transactions = page.content;
        this.totalPages = page.totalPages;
        this.first = page.first;
        this.last = page.last;
        this.loading = false;
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju transakcija.');
        this.loading = false;
      }
    });
  }

  prevPage() {
    if (!this.first) { this.currentPage--; this.load(); }
  }

  nextPage() {
    if (!this.last) { this.currentPage++; this.load(); }
  }

  isPositive(type: string): boolean {
    return type === 'TOPUP_ADMIN' || type === 'ADMIN_ADJUSTMENT';
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'TOPUP_ADMIN': return 'Dopuna';
      case 'PROMOTION_PURCHASE': return 'Promocija';
      case 'ADMIN_ADJUSTMENT': return 'Korekcija';
      default: return type;
    }
  }
}
