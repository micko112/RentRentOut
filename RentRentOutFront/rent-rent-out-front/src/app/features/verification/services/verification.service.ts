import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';

export type VerificationStatusValue = 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';

export interface VerificationStatus {
  status: VerificationStatusValue;
  submittedAt: string | null;
  reviewedAt: string | null;
  rejectionReason: string | null;
}

export interface AdminVerification {
  id: number;
  userId: number;
  userFullName: string;
  userEmail: string;
  status: VerificationStatusValue;
  submittedAt: string;
  reviewedAt: string | null;
  rejectionReason: string | null;
}

export interface AdminVerificationDetails {
  id: number;
  userId: number;
  userFullName: string;
  userEmail: string;
  status: VerificationStatusValue;
  submittedAt: string;
  docFrontUrl: string | null;
  docBackUrl: string | null;
  selfieUrl: string | null;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class VerificationService {

  private url = `${API_BASE_URL}/verification`;
  private adminUrl = `${API_BASE_URL}/admin/verifications`;

  constructor(private http: HttpClient) {}

  // ── User ──
  getMyStatus(): Observable<VerificationStatus> {
    return this.http.get<VerificationStatus>(`${this.url}/status`);
  }

  submit(docFront: File, selfie: File, docBack?: File | null): Observable<VerificationStatus> {
    const formData = new FormData();
    formData.append('docFront', docFront);
    formData.append('selfie', selfie);
    if (docBack) formData.append('docBack', docBack);
    return this.http.post<VerificationStatus>(`${this.url}/submit`, formData);
  }

  // ── Admin ──
  listForAdmin(status: string = 'PENDING', page = 0, size = 20): Observable<Page<AdminVerification>> {
    const params = new HttpParams()
      .set('status', status)
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<AdminVerification>>(this.adminUrl, { params });
  }

  getDetailsForAdmin(id: number): Observable<AdminVerificationDetails> {
    return this.http.get<AdminVerificationDetails>(`${this.adminUrl}/${id}`);
  }

  approve(id: number): Observable<void> {
    return this.http.patch<void>(`${this.adminUrl}/${id}/approve`, {});
  }

  reject(id: number, reason: string): Observable<void> {
    return this.http.patch<void>(`${this.adminUrl}/${id}/reject`, { reason });
  }
}
