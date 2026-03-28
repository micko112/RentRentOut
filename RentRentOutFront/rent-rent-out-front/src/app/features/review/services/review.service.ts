import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Review} from '../../../shared/models/review';
import {Observable} from 'rxjs';
import {Page} from '../../../shared/models/adPreview.model';

export interface CreateReviewPayload {
  contractId: number | null;
  paymentOk: string | null;
  communicationOk: string | null;
  agreementOk: string | null;
  comment: string;
}


@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  constructor(private http: HttpClient) { }


  public getReviewsForUser(userId: number): Observable<Page<Review>> {

    return this.http.get<Page<Review>>(`${API_BASE_URL}/user/${userId}/reviews`);
  }

  getLatestReviewsForUser(userId: number, limit: number = 3): Observable<Page<Review>> {
    let params = new HttpParams()
      .set('page', '0')
      .set('size', limit.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<Page<Review>>(`${API_BASE_URL}/user/${userId}/reviews`, { params });
  }

  submitReview(payload: CreateReviewPayload): Observable<Review> {
    return this.http.post<Review>(`${API_BASE_URL}/reviews`, payload);
  }

  getContractWith(userId: number): Observable<{ contractId: number | null }> {
    return this.http.get<{ contractId: number | null }>(`${API_BASE_URL}/reviews/contract-with/${userId}`);
  }

}
