import {ApplicationConfig, inject, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
import { provideRouter, withInMemoryScrolling, withPreloading, PreloadAllModules, Router } from '@angular/router';
import { provideClientHydration } from '@angular/platform-browser';
import {
  HttpClient,
  HttpInterceptorFn,
  provideHttpClient,
  withFetch,
  withInterceptors
} from '@angular/common/http';
import localeSr from '@angular/common/locales/sr-Latn';

import { routes } from './app.routes';
import {DatePipe, registerLocaleData} from '@angular/common';

import {catchError, switchMap, throwError} from 'rxjs';
import {ToastService} from './shared/services/toast.service';

import { ErrorHandler } from '@angular/core'; // <-- DODAJ IMPORT
import * as Sentry from "@sentry/angular";    // <-- DODAJ IMPORT

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
        const isUserMe   = req.url.includes('/user/me');
        const isRetry    = req.headers.has('X-Is-Retry');
        const isSilent   = req.headers.has('X-Silent');
        const isAuthEndpoint =
          req.url.includes('/auth/refresh') ||
          req.url.includes('/auth/logout')  ||
          req.url.includes('/user/login');

        if (isSilent) return throwError(() => err);

        if (isUserMe) {
          if (isRetry) return throwError(() => err);
          return http.post('/api/auth/refresh', {}, {
            withCredentials: true,
            headers: { 'X-Silent': 'true' }
          }).pipe(
            switchMap(() => next(req.clone({
              withCredentials: true,
              headers: req.headers.set('X-Is-Retry', 'true')
            }))),
            catchError(() => throwError(() => err))
          );
        }


        if (!isAuthEndpoint && !isRetry) {
          return http.post('/api/auth/refresh', {}, { withCredentials: true }).pipe(
            switchMap(() => next(req.clone({
              withCredentials: true,
              headers: req.headers.set('X-Is-Retry', 'true')
            }))),
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
    provideHttpClient(withFetch(), withInterceptors([authInterceptor, errorInterceptor])),
    provideClientHydration(),
    {provide: LOCALE_ID, useValue: 'sr-Latn'},
    DatePipe,
    {
      provide: ErrorHandler,
      useValue: Sentry.createErrorHandler({
        showDialog: false, // Ako staviš 'true', korisniku će iskočiti prozor da opiše šta je radio kad je puklo
      }),
    }
  ]
};
