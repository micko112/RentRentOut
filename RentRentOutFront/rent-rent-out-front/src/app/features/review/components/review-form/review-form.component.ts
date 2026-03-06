import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../../../../core/config/api.config';

@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css']
})
export class ReviewFormComponent implements OnInit {
  @Input() contractId!: number;

  // Stanja komponente
  isChecking = true;
  isEligible = false;
  errorMessage = '';
  isSubmitted = false;

  // Model forme
  reviewData = {
    paymentOk: null,
    communicationOk: null,
    agreementOk: null,
    comment: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    // Čim se komponenta prikaže, proveravamo da li korisnik sme da oceni
    this.http.get<{eligible: boolean, reason: string}>(`${API_BASE_URL}/review/check/${this.contractId}`)
      .subscribe({
        next: (response) => {
          this.isChecking = false;
          this.isEligible = response.eligible;
          this.errorMessage = response.reason;
        },
        error: () => {
          this.isChecking = false;
          this.errorMessage = 'Došlo je do greške pri proveri statusa.';
        }
      });
  }

  submitReview(): void {
    if (!this.reviewData.paymentOk || !this.reviewData.communicationOk || !this.reviewData.agreementOk) {
      alert('Molimo odgovorite na sva tri pitanja.');
      return;
    }

    const payload = {
      contractId: this.contractId,
      ...this.reviewData
    };

    this.http.post(`${API_BASE_URL}/review`, payload).subscribe({
      next: () => {
        this.isSubmitted = true;
      },
      error: (err) => {
        alert('Greška pri slanju ocene: ' + (err.error?.message || ''));
      }
    });
  }
}
