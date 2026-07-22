import {APP_INITIALIZER, ApplicationConfig, inject, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
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

import {catchError, from, switchMap, tap, throwError} from 'rxjs';
import {ToastService} from './shared/services/toast.service';
import {PlatformService} from './core/services/platform.service';

import { ErrorHandler } from '@angular/core'; // <-- DODAJ IMPORT
import * as Sentry from "@sentry/angular";    // <-- DODAJ IMPORT

registerLocaleData(localeSr);

// Dodaje withCredentials na sve zahteve — browser automatski šalje HttpOnly cookie
// Na mobile-u (Capacitor): dodaje X-Client-Platform: mobile i Authorization: Bearer <token>
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platform = inject(PlatformService);

  if (!platform.isNative) {
    return next(req.clone({ withCredentials: true }));
  }

  const cached = platform.getCachedAccess();
  let headers = req.headers.set('X-Client-Platform', 'mobile');
  if (cached && !req.headers.has('Authorization')) {
    headers = headers.set('Authorization', `Bearer ${cached}`);
  }
  return next(req.clone({ withCredentials: true, headers }));
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router      = inject(Router);
  const toastService = inject(ToastService);
  const http        = inject(HttpClient);
  const platform    = inject(PlatformService);

  const refreshCall = () => {
    if (!platform.isNative) {
      return http.post<any>('/api/auth/refresh', {}, {
        withCredentials: true,
        headers: { 'X-Silent': 'true' }
      });
    }
    // Mobile: šalje refresh token u body, hvata nove tokene iz response-a
    return from(platform.getRefreshToken()).pipe(
      switchMap(rt => http.post<any>('/api/auth/refresh',
        { refreshToken: rt },
        { withCredentials: true, headers: { 'X-Silent': 'true', 'X-Client-Platform': 'mobile' } }
      )),
      tap((res: any) => {
        if (res?.accessToken && res?.refreshToken) {
          platform.saveTokens(res.accessToken, res.refreshToken);
        }
      })
    );
  };

  const retry = () => {
    let headers = req.headers.set('X-Is-Retry', 'true');
    if (platform.isNative) {
      const cached = platform.getCachedAccess();
      headers = headers.set('X-Client-Platform', 'mobile');
      if (cached) headers = headers.set('Authorization', `Bearer ${cached}`);
    }
    return next(req.clone({ withCredentials: true, headers }));
  };

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
          return refreshCall().pipe(
            switchMap(() => retry()),
            catchError(() => throwError(() => err))
          );
        }

        if (!isAuthEndpoint && !isRetry) {
          return refreshCall().pipe(
            switchMap(() => retry()),
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
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: (platform: PlatformService) => () => platform.hydrate(),
      deps: [PlatformService],
    },
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
