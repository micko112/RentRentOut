import {APP_INITIALIZER, ApplicationConfig, importProvidersFrom, inject, Injector, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
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
import localeEn from '@angular/common/locales/en';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import {DatePipe, registerLocaleData} from '@angular/common';
import { LanguageService, readLangCookie, INITIAL_LANG, AppLang } from './core/services/language.service';
import { TranslateService } from '@ngx-translate/core';

import {catchError, from, switchMap, tap, throwError} from 'rxjs';
import {ToastService} from './shared/services/toast.service';
import {PlatformService} from './core/services/platform.service';

import { ErrorHandler } from '@angular/core'; // <-- DODAJ IMPORT
import * as Sentry from "@sentry/angular";    // <-- DODAJ IMPORT
import { DOCUMENT } from '@angular/common';

registerLocaleData(localeSr);
registerLocaleData(localeEn);

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}

// Dodaje withCredentials na sve zahteve — browser automatski šalje HttpOnly cookie
// Na mobile-u (Capacitor): dodaje X-Client-Platform: mobile i Authorization: Bearer <token>
// Jezik čita iz localStorage (browser) ili iz `rro_lang` cookie-ja (SSR, gde
// localStorage ne postoji). Bez cookie fallback-a SSR bi uvek slao Accept-Language: sr,
// pa bi TransferState klijentu isporučio srpske kategorije čak i kada je izabran EN.
function currentLangHeader(doc: Document | null, ssrLang: AppLang | null): string {
  if (ssrLang) return ssrLang;
  try {
    if (typeof localStorage !== 'undefined') {
      const v = localStorage.getItem('rro_lang');
      if (v === 'en') return 'en';
      if (v === 'sr') return 'sr';
    }
  } catch { /* ignore */ }
  return readLangCookie(doc) ?? 'sr';
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platform = inject(PlatformService);
  const doc = inject(DOCUMENT, { optional: true });
  const ssrLang = inject(INITIAL_LANG, { optional: true });
  const lang = currentLangHeader(doc, ssrLang);

  if (!platform.isNative) {
    const headers = req.headers.has('Accept-Language')
      ? req.headers
      : req.headers.set('Accept-Language', lang);
    return next(req.clone({ withCredentials: true, headers }));
  }

  const cached = platform.getCachedAccess();
  let headers = req.headers.set('X-Client-Platform', 'mobile');
  if (!req.headers.has('Accept-Language')) {
    headers = headers.set('Accept-Language', lang);
  }
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
  const injector    = inject(Injector);
  const tr = (key: string): string => {
    try {
      const t = injector.get(TranslateService, null as any);
      return t ? t.instant(key) : key;
    } catch {
      return key;
    }
  };

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
        toastService.showError(tr('toast.no_permission'));
      } else if (err.status >= 500) {
        toastService.showError(tr('toast.server_error'));
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
    importProvidersFrom(
      TranslateModule.forRoot({
        defaultLanguage: 'sr',
        loader: {
          provide: TranslateLoader,
          useFactory: createTranslateLoader,
          deps: [HttpClient],
        },
      })
    ),
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: (platform: PlatformService) => () => platform.hydrate(),
      deps: [PlatformService],
    },
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: (lang: LanguageService) => () => lang.init(),
      deps: [LanguageService],
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
