import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Location} from '../../../shared/models/location.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private url = `${API_BASE_URL}/locations`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Location[]> {
    return this.http.get<Location[]>(this.url);
  }
}