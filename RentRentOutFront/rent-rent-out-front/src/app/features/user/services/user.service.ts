import { Injectable } from '@angular/core';
import {API_BASE_URL} from '../../../core/config/api.config';
import {HttpClient} from '@angular/common/http';
import {Observable, map} from 'rxjs';
import {User} from '../../../shared/models/user.model';
import {RentalContract} from '../../../shared/models/rental-contract.model';
import {UserProfile} from '../../../shared/models/userProfile';
import {PublicProfile} from '../../../shared/models/public-profile';
import {UpdateUserRequest} from '../../../shared/models/update-user-request';
import {ChangePasswordRequest} from '../../../shared/models/change-password-request';


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
  getPhoneNumber(userId: number): Observable<{ phone: string }> {
    return this.http.get<{ phone: string }>(`${this.url}/user/${userId}/phone`);
  }

  updateMe(payload: UpdateUserRequest): Observable<User> {
    return this.http.patch<{ user: User }>(`${this.url}/user/me`, payload)
      .pipe(map(res => res.user));
  }

  changePassword(payload: ChangePasswordRequest): Observable<string> {
    return this.http.patch(`${this.url}/user/me/password`, payload, { responseType: 'text' });
  }

  uploadAvatar(file: File): Observable<string[]> {
    const formData = new FormData();
    formData.append('files', file);
    return this.http.post<string[]>(`${this.url}/images/upload`, formData);
  }
}
