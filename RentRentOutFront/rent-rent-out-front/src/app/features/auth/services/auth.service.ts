import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from '../../../shared/models/user.model';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Router} from '@angular/router';
import {RegisterRequest} from '../../../shared/models/register.model';
import {PlatformService} from '../../../core/services/platform.service';

interface LoginResponse {
  user: User;
  wsToken: string;
  accessToken?: string;   // mobile only
  refreshToken?: string;  // mobile only
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  // In-memory samo — za WebSocket STOMP header; NE čuva se u localStorage
  wsToken: string | null = null;

  constructor(private http: HttpClient, private router: Router, private platform: PlatformService) {
    this.loadInitialUser();
  }

  private handleLoginResponse(res: LoginResponse): void {
    // Cache tokena MORA biti pre currentUser$ emit-a — inače subscriberi mogu poslati HTTP bez Bearer header-a
    if (this.platform.isNative && res.accessToken && res.refreshToken) {
      this.platform.saveTokens(res.accessToken, res.refreshToken);
    }
    this.wsToken = res.wsToken;
    this.currentUserSubject.next(res.user);
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }

  login(credentials: { email: string; password: string }): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/user/login`, credentials, { withCredentials: true })
      .pipe(tap(res => { this.handleLoginResponse(res); }));
  }

  logout(): void {
    this.http.post(`${API_BASE_URL}/auth/logout`, {}, { withCredentials: true }).subscribe({
      complete: () => this.clearSession(),
      error:    () => this.clearSession(),
    });
  }

  private clearSession(): void {
    this.currentUserSubject.next(null);
    this.wsToken = null;
    this.platform.clearTokens();
    this.router.navigate(['/']);
  }

  register(userData: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${API_BASE_URL}/user/register`, userData, { withCredentials: true });
  }

  refresh(): Observable<{ wsToken: string; accessToken?: string; refreshToken?: string }> {
    return this.http
      .post<{ wsToken: string; accessToken?: string; refreshToken?: string }>(
        `${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true })
      .pipe(tap(res => {
        this.wsToken = res.wsToken;
        if (this.platform.isNative && res.accessToken && res.refreshToken) {
          this.platform.saveTokens(res.accessToken, res.refreshToken);
        }
      }));
  }

  verifyEmail(token: string): Observable<User> {
    return this.http.get<User>(`${API_BASE_URL}/auth/validate-email`, { params: { token }, withCredentials: true });
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${API_BASE_URL}/auth/forgot-password`, { email }, { responseType: 'text', withCredentials: true });
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${API_BASE_URL}/auth/reset-password`, { token, newPassword }, { responseType: 'text', withCredentials: true });
  }

  googleLogin(idToken: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/user/google-login`, { idToken }, { withCredentials: true })
      .pipe(tap(res => { this.handleLoginResponse(res); }));
  }

  facebookLogin(accessToken: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/user/facebook-login`, { accessToken }, { withCredentials: true })
      .pipe(tap(res => { this.handleLoginResponse(res); }));
  }

  appleLogin(identityToken: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/user/apple-login`, { identityToken }, { withCredentials: true })
      .pipe(tap(res => { this.handleLoginResponse(res); }));
  }

  private loadInitialUser(): void {
    // Token je već pre-loadovan kroz APP_INITIALIZER → PlatformService.hydrate()
    this.http.get<User>(`${API_BASE_URL}/user/me`, { withCredentials: true }).subscribe({
      next: user => {
        this.currentUserSubject.next(user);
        this.http.get<{ wsToken: string }>(`${API_BASE_URL}/auth/ws-token`, { withCredentials: true }).subscribe({
          next: res => { this.wsToken = res.wsToken; },
          error: () => {},
        });
      },
      error: () => {},
    });
  }
}
