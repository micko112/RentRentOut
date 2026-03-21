import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ReviewService} from '../../services/review.service';
import {ToastService} from '../../../../shared/services/toast.service';


@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css']
})
export class ReviewFormComponent implements OnInit {
  @Input() contractId!: number | null;
  @Output() reviewSaved = new EventEmitter<void>();

  reviewData = {
    paymentOk: null as string | null,
    communicationOk: null as string | null,
    agreementOk: null as string | null,
    comment: ''
  };

  constructor(
    private reviewService: ReviewService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {}

  submitReview(): void {
    if (!this.reviewData.paymentOk || !this.reviewData.communicationOk || !this.reviewData.agreementOk) {
      this.toastService.showError('Morate odgovoriti na sva tri pitanja!');
      return;
    }

    const payload = {
      contractId: this.contractId,
      paymentOk: this.reviewData.paymentOk,
      communicationOk: this.reviewData.communicationOk,
      agreementOk: this.reviewData.agreementOk,
      comment: this.reviewData.comment
    };

    this.reviewService.submitReview(payload).subscribe({
      next: () => {
        this.toastService.showSuccess('Ocena je uspešno sačuvana!');
        this.reviewSaved.emit();
      },
      error: (err) => {
        this.toastService.showError(err?.error?.message || 'Greška pri čuvanju ocene.');
      }
    });
  }
}
