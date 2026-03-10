import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
  @Input() contractId!: number | null;
  @Output() reviewSaved = new EventEmitter<void>();



  reviewData = {
    paymentOk: null,
    communicationOk: null,
    agreementOk: null,
    comment: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {

  }

  submitReview(): void {
    console.log('Review submitted' + this.reviewData.paymentOk + " "  + this.reviewData.communicationOk + " "  +  this.reviewData.agreementOk);
    if(!this.reviewData.paymentOk || !this.reviewData.communicationOk  ||  !this.reviewData.agreementOk){
      alert("Morate odgovoriti na sva tri pitanja!");
      return;
    }

    const payload = {
      contractId: this.contractId,
      paymentOk: this.reviewData.paymentOk,
      communicationOk: this.reviewData.communicationOk,
      agreementOk: this.reviewData.agreementOk,
      comment: this.reviewData.comment
    }

    this.http.post(`${API_BASE_URL}/reviews`, payload).subscribe({
      next: () => {
        this.reviewSaved.emit();
      },
      error: (err) => {
        alert("Greska: " + err.error.message);
      }
    });

  }
}
