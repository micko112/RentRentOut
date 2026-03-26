import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PromotionService, CreditBalance, CreditTransaction } from '../../../ads/services/promotion.service';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-credit',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './credit.component.html',
  styleUrl: './credit.component.css'
})
export class CreditComponent implements OnInit {
  balance: number | null = null;
  transactions: CreditTransaction[] = [];
  loading = true;
  loadingHistory = true;

  constructor(
    private promotionService: PromotionService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.promotionService.getCreditBalance().subscribe({
      next: (b) => { this.balance = b.balance; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.promotionService.getCreditHistory().subscribe({
      next: (page) => { this.transactions = page.content; this.loadingHistory = false; },
      error: () => { this.loadingHistory = false; }
    });
  }

  transactionLabel(type: string): string {
    switch (type) {
      case 'TOPUP_ADMIN': return 'Dopuna kredita';
      case 'PROMOTION_PURCHASE': return 'Aktivacija promocije';
      case 'ADMIN_ADJUSTMENT': return 'Admin korekcija';
      default: return type;
    }
  }

  isPositive(type: string): boolean {
    return type === 'TOPUP_ADMIN' || type === 'ADMIN_ADJUSTMENT';
  }
}
