import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Review} from '../../../shared/models/review';
import {Observable} from 'rxjs';
import {Page} from '../../../shared/models/adPreview.model';
import * as http from 'node:http';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  constructor(private http: HttpClient) { }


  public getReviewsForUser(userId: number): Observable<Page<Review>> {

    return this.http.get<Page<Review>>(`${API_BASE_URL}/user/${userId}/reviews`);
  }



}
