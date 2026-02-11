import {ApplicationConfig, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  HttpClientModule, HttpEvent, HttpHandler,
  HttpInterceptor, HttpInterceptorFn,
  HttpRequest,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import localeSr from '@angular/common/locales/sr-Latn';

import { routes } from './app.routes';
import {DatePipe, registerLocaleData} from '@angular/common';

import {Observable} from 'rxjs';

registerLocaleData(localeSr)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // const authService = inject(AuthService); // <-- Ovako bi injektovao servis ako ti treba

  const token = localStorage.getItem('authToken');

  if (token) {
    const cloned = req.clone({
      setHeaders: {Authorization: `Bearer ${token}`},
    });
    return next(cloned);
  }

  return next(req);
};

export const appConfig: ApplicationConfig = {
  providers:
    [provideZoneChangeDetection({ eventCoalescing: true }),
      provideRouter(routes),
      provideHttpClient(withInterceptors([authInterceptor])),
      {provide: LOCALE_ID, useValue: 'sr-Latn'},
    DatePipe
    ]
};
