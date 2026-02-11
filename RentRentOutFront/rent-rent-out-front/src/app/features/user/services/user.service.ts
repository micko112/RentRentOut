import { Injectable } from '@angular/core';
import {API_BASE_URL} from '../../../core/config/api.config';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private url = `${API_BASE_URL}/user`
  constructor(private http: HttpClient) { }

  getMe(id: number): Observable<User> {
    return this.http.get<User>(`${this.url}/my-profile`, id);
  }
}
