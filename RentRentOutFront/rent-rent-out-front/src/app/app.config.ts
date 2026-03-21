import {ApplicationConfig, inject, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
import { provideRouter, withInMemoryScrolling, withPreloading, PreloadAllModules, Router } from '@angular/router';
import {
  HttpInterceptorFn,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import localeSr from '@angular/common/locales/sr-Latn';

import { routes } from './app.routes';
import {DatePipe, registerLocaleData} from '@angular/common';

import {catchError, throwError} from 'rxjs';
import {ToastService} from './shared/services/toast.service';

registerLocaleData(localeSr);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');

  if (token) {
    const cloned = req.clone({
      setHeaders: {Authorization: `Bearer ${token}`},
    });
    return next(cloned);
  }

  return next(req);
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError(err => {
      if (err.status === 401) {
        localStorage.removeItem('authToken');
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
  providers:
    [provideZoneChangeDetection({ eventCoalescing: true }),
      provideRouter(routes, withPreloading(PreloadAllModules), withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
      provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
      {provide: LOCALE_ID, useValue: 'sr-Latn'},
    DatePipe
    ]
};
