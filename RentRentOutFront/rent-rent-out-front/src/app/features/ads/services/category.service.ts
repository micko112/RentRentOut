import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Category } from '../../../shared/models/category.model';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { API_BASE_URL } from '../../../core/config/api.config';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private all$ = this.http.get<Category[]>(`${API_BASE_URL}/categories`).pipe(shareReplay(1));

  constructor(private http: HttpClient) { }

  getAll(): Observable<Category[]> {
    return this.all$;
  }

  suggestCategory(title: string): Observable<number> {
    let params = new HttpParams().set('title', title);
    return this.http.get<number>(`${API_BASE_URL}/categories/suggest`, { params });
  }

}
