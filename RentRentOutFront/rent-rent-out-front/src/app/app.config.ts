import {ApplicationConfig, inject, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
import { provideRouter, withInMemoryScrolling, withPreloading, PreloadAllModules, Router } from '@angular/router';
import {
  HttpClient,
  HttpInterceptorFn,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import localeSr from '@angular/common/locales/sr-Latn';

import { routes } from './app.routes';
import {DatePipe, registerLocaleData} from '@angular/common';

import {catchError, switchMap, throwError} from 'rxjs';
import {ToastService} from './shared/services/toast.service';

registerLocaleData(localeSr);

// Dodaje withCredentials na sve zahteve — browser automatski šalje HttpOnly cookie
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req.clone({ withCredentials: true }));
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router      = inject(Router);
  const toastService = inject(ToastService);
  const http        = inject(HttpClient);

  return next(req).pipe(
    catchError(err => {
      if (err.status === 401) {
        // Ne pokušavaj refresh na auth endpointima — izbegava petlju
        const isAuthEndpoint =
          req.url.includes('/auth/refresh') ||
          req.url.includes('/auth/logout')  ||
          req.url.includes('/user/me')       ||
          req.url.includes('/user/login');

        if (!isAuthEndpoint) {
          return http.post('/api/auth/refresh', {}, { withCredentials: true }).pipe(
            switchMap(() => next(req.clone({ withCredentials: true }))),
            catchError(() => {
              router.navigate(['/login']);
              return throwError(() => err);
            })
          );
        }
        router.navigate(['/login']);
      } else if (err.status === 403) {
        toastService.showError('Nemate dozvolu za ovu akciju.');
      } else if (err.status >= 500) {
        toastService.showError('Greška na serveru. Pokušajte ponovo.');
      }
      return throwError(() => err);
    })
  );
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withPreloading(PreloadAllModules), withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    {provide: LOCALE_ID, useValue: 'sr-Latn'},
    DatePipe,
  ]
};
