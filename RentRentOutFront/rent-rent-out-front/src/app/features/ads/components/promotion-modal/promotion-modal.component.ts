import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PromotionService, PromotionPackage, CreditBalance } from '../../services/promotion.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { PromotionType } from '../../../../shared/models/adPreview.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-promotion-modal',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './promotion-modal.component.html',
  styleUrl: './promotion-modal.component.css'
})
export class PromotionModalComponent implements OnInit {
  @Input() adId!: number;
  @Input() adTitle = '';
  @Output() closed = new EventEmitter<void>();
  @Output() activated = new EventEmitter<void>();

  packages: PromotionPackage[] = [];
  balance = 0;
  selectedType: PromotionType | null = null;
  loading = true;
  activating = false;

  constructor(
    private promotionService: PromotionService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    forkJoin({
      packages: this.promotionService.getPackages(),
      balance: this.promotionService.getCreditBalance()
    }).subscribe({
      next: ({ packages, balance }) => {
        this.packages = packages;
        this.balance = balance.balance;
        this.loading = false;
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju paketa.');
        this.loading = false;
      }
    });
  }

  select(type: PromotionType): void {
    this.selectedType = type;
  }

  get selectedPackage(): PromotionPackage | null {
    return this.packages.find(p => p.type === this.selectedType) ?? null;
  }

  get canActivate(): boolean {
    return !!this.selectedPackage && this.balance >= (this.selectedPackage?.priceRsd ?? Infinity);
  }

  activate(): void {
    if (!this.selectedType || this.activating) return;
    this.activating = true;
    this.promotionService.activate(this.adId, this.selectedType).subscribe({
      next: () => {
        this.toastService.showSuccess('Promocija je aktivirana!');
        this.activated.emit();
        this.close();
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Greška pri aktivaciji promocije.';
        this.toastService.showError(msg);
        this.activating = false;
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}
