import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from '../../../shared/models/user.model';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Router} from '@angular/router';
import {RegisterRequest} from '../../../shared/models/register.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  // In-memory samo — za WebSocket STOMP header; NE čuva se u localStorage
  wsToken: string | null = null;

  constructor(private http: HttpClient, private router: Router) {
    this.loadInitialUser();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }

  login(credentials: { email: string; password: string }): Observable<{ user: User; wsToken: string }> {
    return this.http
      .post<{ user: User; wsToken: string }>(`${API_BASE_URL}/user/login`, credentials, { withCredentials: true })
      .pipe(tap(res => {
        this.currentUserSubject.next(res.user);
        this.wsToken = res.wsToken;
      }));
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
    this.router.navigate(['/']);
  }

  register(userData: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${API_BASE_URL}/user/register`, userData, { withCredentials: true });
  }

  refresh(): Observable<{ wsToken: string }> {
    return this.http
      .post<{ wsToken: string }>(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true })
      .pipe(tap(res => { this.wsToken = res.wsToken; }));
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

  googleLogin(idToken: string): Observable<{ user: User; wsToken: string }> {
    return this.http
      .post<{ user: User; wsToken: string }>(`${API_BASE_URL}/user/google-login`, { idToken }, { withCredentials: true })
      .pipe(tap(res => {
        this.currentUserSubject.next(res.user);
        this.wsToken = res.wsToken;
      }));
  }

  facebookLogin(accessToken: string): Observable<{ user: User; wsToken: string }> {
    return this.http
      .post<{ user: User; wsToken: string }>(`${API_BASE_URL}/user/facebook-login`, { accessToken }, { withCredentials: true })
      .pipe(tap(res => {
        this.currentUserSubject.next(res.user);
        this.wsToken = res.wsToken;
      }));
  }

  appleLogin(identityToken: string): Observable<{ user: User; wsToken: string }> {
    return this.http
      .post<{ user: User; wsToken: string }>(`${API_BASE_URL}/user/apple-login`, { identityToken }, { withCredentials: true })
      .pipe(tap(res => {
        this.currentUserSubject.next(res.user);
        this.wsToken = res.wsToken;
      }));
  }

  private loadInitialUser(): void {
    // Cookie se automatski šalje — ne treba localStorage provjera
    this.http.get<User>(`${API_BASE_URL}/user/me`, { withCredentials: true }).subscribe({
      next: user => {
        this.currentUserSubject.next(user);
        // Dohvati wsToken za WebSocket konekciju
        this.http.get<{ wsToken: string }>(`${API_BASE_URL}/auth/ws-token`, { withCredentials: true }).subscribe({
          next: res => { this.wsToken = res.wsToken; },
        });
      },
      // 401 = nije ulogovan, to je OK — ignorišemo grešku
    });
  }
}
