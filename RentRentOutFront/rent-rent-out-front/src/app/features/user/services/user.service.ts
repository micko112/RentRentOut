import { Injectable } from '@angular/core';
import {API_BASE_URL} from '../../../core/config/api.config';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {User} from '../../../shared/models/user.model';
import {Ad} from '../../../shared/models/ad.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private url = `${API_BASE_URL}/user`
  constructor(private http: HttpClient) { }

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.url}/me`);
  }

}
