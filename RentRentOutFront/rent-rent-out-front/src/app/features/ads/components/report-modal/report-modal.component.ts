import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdService } from '../../services/ad.service';
import { ToastService } from '../../../../shared/services/toast.service';

export interface ReportReason {
  value: string;
  key: string;
}

export const REPORT_REASONS: ReportReason[] = [
  { value: 'Netačne informacije',   key: 'report.reason_wrong_info' },
  { value: 'Prevara ili spam',      key: 'report.reason_fraud_spam' },
  { value: 'Neprikladan sadržaj',   key: 'report.reason_inappropriate' },
  { value: 'Duplikat oglasa',       key: 'report.reason_duplicate' },
  { value: 'Ostalo',                key: 'report.reason_other' },
];

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './report-modal.component.html',
  styleUrl: './report-modal.component.css',
})
export class ReportModalComponent {
  @Input() adId!: number;
  @Output() closed = new EventEmitter<void>();

  readonly reasons = REPORT_REASONS;
  selectedReason = '';
  note = '';
  submitting = false;

  constructor(private adService: AdService, private toast: ToastService, private translate: TranslateService) {}

  submit() {
    if (!this.selectedReason) return;
    this.submitting = true;
    this.adService.reportAd(this.adId, this.selectedReason, this.note).subscribe({
      next: () => {
        this.toast.showSuccess(this.translate.instant('report.toast_sent'));
        this.closed.emit();
      },
      error: (err) => {
        this.toast.showError(err?.error || this.translate.instant('report.toast_error'));
        this.submitting = false;
      },
    });
  }

  close() {
    this.closed.emit();
  }
}
