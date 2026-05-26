import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';

export interface AdTemplate {
  id: number;
  name: string;
  data: { [key: string]: any };
  createdAt: string;
  updatedAt: string;
}

export interface AdTemplatePayload {
  name: string;
  data: { [key: string]: any };
}

@Injectable({ providedIn: 'root' })
export class AdTemplateService {
  private base = `${API_BASE_URL}/templates`;

  private templates$ = new BehaviorSubject<AdTemplate[]>([]);
  private loaded = false;

  readonly list$ = this.templates$.asObservable();

  constructor(private http: HttpClient) {}

  /** Učitaj listu šablona — keširano dok se ne pozove refresh(). */
  ensureLoaded(): void {
    if (this.loaded) return;
    this.refresh();
  }

  refresh(): void {
    this.http.get<AdTemplate[]>(this.base).subscribe({
      next: list => {
        this.templates$.next(list);
        this.loaded = true;
      },
      error: () => {
        this.templates$.next([]);
        this.loaded = true;
      },
    });
  }

  clear(): void {
    this.templates$.next([]);
    this.loaded = false;
  }

  get(id: number): AdTemplate | undefined {
    return this.templates$.value.find(t => t.id === id);
  }

  create(payload: AdTemplatePayload): Observable<AdTemplate> {
    return this.http.post<AdTemplate>(this.base, payload).pipe(
      tap(t => this.templates$.next([t, ...this.templates$.value]))
    );
  }

  update(id: number, payload: Partial<AdTemplatePayload>): Observable<AdTemplate> {
    return this.http.put<AdTemplate>(`${this.base}/${id}`, payload).pipe(
      tap(updated => {
        const list = this.templates$.value.map(t => t.id === id ? updated : t);
        this.templates$.next(list);
      })
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      tap(() => this.templates$.next(this.templates$.value.filter(t => t.id !== id)))
    );
  }
}
