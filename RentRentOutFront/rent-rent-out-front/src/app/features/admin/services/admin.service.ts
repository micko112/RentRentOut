import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { User } from '../../../shared/models/user.model';
import { AdPreview, Page } from '../../../shared/models/adPreview.model';
import { RentalContract } from '../../../shared/models/rental-contract.model';

export interface AdminStats {
  totalUsers: number;
  totalAds: number;
  activeAds: number;
  totalContracts: number;
  activeContracts: number;
  pendingReports: number;
}

export interface AdReport {
  id: number;
  adId: number;
  adTitle: string;
  reporterId: number;
  reporterName: string;
  reason: string;
  note: string;
  reviewed: boolean;
  createdAt: string;
}

export interface UserCreditSummary {
  userId: number;
  fullName: string;
  email: string;
  currentBalance: number;
  totalSpent: number;
  totalTopups: number;
  lastTransactionAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private url = `${API_BASE_URL}/admin`;

  constructor(private http: HttpClient) {}

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.url}/stats`);
  }

  getUsers(page: number = 0, size: number = 20): Observable<Page<User>> {
    return this.http.get<Page<User>>(`${this.url}/users`, {
      params: { page, size }
    });
  }

  getAds(page: number = 0, size: number = 20): Observable<Page<AdPreview>> {
    return this.http.get<Page<AdPreview>>(`${this.url}/ads`, {
      params: { page, size }
    });
  }

  getContracts(page: number = 0, size: number = 20): Observable<Page<RentalContract>> {
    return this.http.get<Page<RentalContract>>(`${this.url}/contracts`, {
      params: { page, size }
    });
  }

  toggleUserEnabled(userId: number): Observable<string> {
    return this.http.patch(`${this.url}/users/${userId}/disable`, {}, { responseType: 'text' });
  }

  suspendAd(adId: number): Observable<string> {
    return this.http.patch(`${this.url}/ads/${adId}/suspend`, {}, { responseType: 'text' });
  }

  unsuspendAd(adId: number): Observable<string> {
    return this.http.patch(`${this.url}/ads/${adId}/unsuspend`, {}, { responseType: 'text' });
  }

  getUserCreditSummaries(page: number = 0, size: number = 20, search: string = ''): Observable<Page<UserCreditSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<UserCreditSummary>>(`${this.url}/credits`, { params });
  }

  getReports(page: number = 0, size: number = 25, onlyUnreviewed: boolean = true): Observable<Page<AdReport>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('onlyUnreviewed', onlyUnreviewed);
    return this.http.get<Page<AdReport>>(`${this.url}/reports`, { params });
  }

  markReportReviewed(reportId: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/reports/${reportId}/reviewed`, {});
  }
}
