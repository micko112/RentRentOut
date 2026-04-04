import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { API_BASE_URL } from '../../../core/config/api.config';
import { Location } from '../../../shared/models/location.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private http = inject(HttpClient);
  private all$ = this.http.get<Location[]>(`${API_BASE_URL}/locations`).pipe(shareReplay(1));

  getAll(): Observable<Location[]> {
    return this.all$;
  }
}