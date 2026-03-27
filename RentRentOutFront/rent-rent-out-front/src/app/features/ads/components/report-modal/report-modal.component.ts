import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdService } from '../../services/ad.service';
import { ToastService } from '../../../../shared/services/toast.service';

export const REPORT_REASONS = [
  'Netačne informacije',
  'Prevara ili spam',
  'Neprikladan sadržaj',
  'Duplikat oglasa',
  'Ostalo',
];

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  constructor(private adService: AdService, private toast: ToastService) {}

  submit() {
    if (!this.selectedReason) return;
    this.submitting = true;
    this.adService.reportAd(this.adId, this.selectedReason, this.note).subscribe({
      next: () => {
        this.toast.showSuccess('Prijava je uspešno poslata. Hvala!');
        this.closed.emit();
      },
      error: (err) => {
        this.toast.showError(err?.error || 'Greška pri slanju prijave.');
        this.submitting = false;
      },
    });
  }

  close() {
    this.closed.emit();
  }
}
