import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { PromotionType } from '../../../shared/models/adPreview.model';

export interface PromotionPackage {
  type: PromotionType;
  displayName: string;
  priceRsd: number;
  durationDays: number;
  description: string;
}

export interface ActivePromotion {
  id: number;
  promotionType: PromotionType;
  displayName: string;
  startsAt: string;
  expiresAt: string;
  pricePaid: number;
}

export interface CreditBalance {
  balance: number;
}

export interface CreditTransaction {
  id: number;
  amount: number;
  transactionType: string;
  description: string;
  referenceId: number | null;
  createdAt: string;
}

export interface AdminCreditTransaction extends CreditTransaction {
  userId: number;
  userEmail: string;
  userName: string;
}

export interface CreditTransactionPage {
  content: CreditTransaction[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private base = `${API_BASE_URL}/promotions`;

  constructor(private http: HttpClient) {}

  getPackages(): Observable<PromotionPackage[]> {
    return this.http.get<PromotionPackage[]>(`${this.base}/packages`);
  }

  activate(adId: number, promotionType: PromotionType): Observable<void> {
    return this.http.post<void>(`${this.base}/activate`, { adId, promotionType });
  }

  getActivePromotions(adId: number): Observable<ActivePromotion[]> {
    return this.http.get<ActivePromotion[]>(`${this.base}/ad/${adId}`);
  }

  renewAd(adId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/renew/${adId}`, {});
  }

  getCreditBalance(): Observable<CreditBalance> {
    return this.http.get<CreditBalance>(`${this.base}/credit`);
  }

  getCreditHistory(page = 0, size = 20): Observable<CreditTransactionPage> {
    return this.http.get<CreditTransactionPage>(`${this.base}/credit/history`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  addCredit(userId: number, amount: number, description: string): Observable<void> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('amount', amount.toString())
      .set('description', description || 'Dopuna kredita');
    return this.http.post<void>(`${this.base}/admin/credit`, null, { params });
  }

  getAllTransactions(page = 0, size = 30): Observable<{ content: AdminCreditTransaction[]; totalPages: number; number: number; first: boolean; last: boolean }> {
    return this.http.get<any>(`${this.base}/admin/transactions`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }
}
