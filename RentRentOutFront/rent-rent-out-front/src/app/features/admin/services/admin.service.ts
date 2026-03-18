import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { User } from '../../../shared/models/user.model';
import { AdPreview, Page } from '../../../shared/models/adPreview.model';
import { RentalContract } from '../../../shared/models/rental-contract.model';

export interface AdminStats {
  totalUsers: number;
  totalAds: number;
  totalContracts: number;
  activeContracts: number;
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
}
