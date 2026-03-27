import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PromotionService, CreditTransaction } from '../../../ads/services/promotion.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { AuthService } from '../../../auth/services/auth.service';

// TODO: Zameniti pravim podacima firme pre lansiranja
const COMPANY_NAME = 'Dimitrije Mitic';
const BANK_ACCOUNT  = '000-0000000000-00';
const PAYMENT_CODE  = '221';
const PAYMENT_MODEL = '97';
const PAYMENT_PURPOSE = 'Dopuna kredita Izdajem Iznajmljujem';

@Component({
  selector: 'app-credit',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './credit.component.html',
  styleUrl: './credit.component.css'
})
export class CreditComponent implements OnInit {
  balance: number | null = null;
  transactions: CreditTransaction[] = [];
  loading = true;
  loadingHistory = true;

  readonly quickAmounts = [500, 1000, 2000, 5000];
  selectedAmount = 1000;
  customAmount: number | null = null;
  showCustom = false;

  copied: Record<string, boolean> = {};

  constructor(
    private promotionService: PromotionService,
    private toastService: ToastService,
    private authService: AuthService
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

  get effectiveAmount(): number {
    return this.showCustom && this.customAmount && this.customAmount >= 100
      ? this.customAmount
      : this.selectedAmount;
  }

  get referenceNumber(): string {
    const userId = this.authService.currentUserValue?.id ?? 0;
    return `10-${String(userId).padStart(10, '0')}`;
  }

  get paymentRows(): { label: string; value: string; key: string }[] {
    return [
      { label: 'Primalac',        value: COMPANY_NAME,                               key: 'primalac' },
      { label: 'Iznos',           value: `${this.effectiveAmount.toLocaleString('sr-RS')} RSD`, key: 'iznos' },
      { label: 'Šifra plaćanja',  value: PAYMENT_CODE,                               key: 'sifra' },
      { label: 'Račun primaoca',  value: BANK_ACCOUNT,                               key: 'racun' },
      { label: 'Model',           value: PAYMENT_MODEL,                              key: 'model' },
      { label: 'Poziv na broj',   value: this.referenceNumber,                       key: 'poziv' },
      { label: 'Svrha uplate',    value: PAYMENT_PURPOSE,                            key: 'svrha' },
    ];
  }

  selectAmount(amount: number): void {
    this.selectedAmount = amount;
    this.showCustom = false;
    this.customAmount = null;
  }

  toggleCustom(): void {
    this.showCustom = true;
    this.customAmount = null;
  }

  copyField(key: string, value: string): void {
    navigator.clipboard.writeText(value).then(() => {
      this.copied[key] = true;
      setTimeout(() => { this.copied[key] = false; }, 1800);
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
