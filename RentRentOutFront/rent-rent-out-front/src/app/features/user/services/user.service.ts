import { Injectable } from '@angular/core';
import {API_BASE_URL} from '../../../core/config/api.config';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {User} from '../../../shared/models/user.model';
import {Ad} from '../../../shared/models/ad.model';
import {RentalContract} from '../../../shared/models/rental-contract.model';
import {UserProfile} from '../../../shared/models/userProfile';
import {PublicProfile} from '../../../shared/models/public-profile';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  private url = `${API_BASE_URL}`
  constructor(private http: HttpClient) { }

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.url}/user/me`);
  }
  getAllContract(): Observable<RentalContract[]> {
    return this.http.get<RentalContract[]>(`${this.url}/rental-contract/my-contracts`);
  }
  getUserProfile(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.url}/user/${id}/profile`);
  }

  getPublicProfile(id: number): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.url}/user/${id}`);
  }

}
