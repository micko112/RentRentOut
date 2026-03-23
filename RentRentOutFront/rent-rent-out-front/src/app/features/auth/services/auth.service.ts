import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from '../../../shared/models/user.model';
import {BehaviorSubject, Observable, shareReplay, tap} from 'rxjs';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Router} from '@angular/router';
import {RegisterRequest} from '../../../shared/models/register.model';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(null);
// $ je znak da je Observable
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient,
              private router: Router,){
    this.loadInitialUser();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }

  login(credentials: { email: string, password: string }): Observable<{ user: User, token: string }> {
    return this.http.post<{ user: User, token: string }>(`${API_BASE_URL}/user/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('authToken', response.token);
          this.currentUserSubject.next(response.user);
        })
      )
  }

  logout() {
    localStorage.removeItem('authToken');
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  register(userData: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${API_BASE_URL}/user/register`, userData);
  }

  verifyEmail(token: string): Observable<User> {
    return this.http.get<User>(`${API_BASE_URL}/auth/validate-email`, { params: { token } });
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${API_BASE_URL}/auth/forgot-password`, { email }, { responseType: 'text' });
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${API_BASE_URL}/auth/reset-password`, { token, newPassword }, { responseType: 'text' });
  }

  googleLogin(idToken: string): Observable<{ user: User, token: string }> {
    return this.http.post<{ user: User, token: string }>(`${API_BASE_URL}/user/google-login`, { idToken })
      .pipe(
        tap(response => {
          localStorage.setItem('authToken', response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }

  facebookLogin(accessToken: string): Observable<{ user: User, token: string }> {
    return this.http.post<{ user: User, token: string }>(`${API_BASE_URL}/user/facebook-login`, { accessToken })
      .pipe(
        tap(response => {
          localStorage.setItem('authToken', response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }

  appleLogin(identityToken: string): Observable<{ user: User, token: string }> {
    return this.http.post<{ user: User, token: string }>(`${API_BASE_URL}/user/apple-login`, { identityToken })
      .pipe(
        tap(response => {
          localStorage.setItem('authToken', response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }

  private loadInitialUser() {
    const token = localStorage.getItem('authToken');

    if (token) {
      this.http.get<User>(`${API_BASE_URL}/user/me`).subscribe({
        next: (user) => {
          console.log('Auto-login uspešan:', user);
          this.currentUserSubject.next(user);
        },
        error: (err) => {
          console.log('Token je istekao ili nevalidan. Auto-logout.');
          this.logout();
        }
      })
    }
  }

}
