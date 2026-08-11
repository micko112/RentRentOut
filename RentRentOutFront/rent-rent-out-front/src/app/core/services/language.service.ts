import { Injectable, InjectionToken, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

export type AppLang = 'sr' | 'en';

const STORAGE_KEY = 'rro_lang';
const SUPPORTED: AppLang[] = ['sr', 'en'];
const DEFAULT_LANG: AppLang = 'sr';
const COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // 1 godina

/**
 * Postavlja se u server.ts iz `Cookie` header-a. `null` u browser-u.
 * Bez ovoga SSR ne bi znao izabrani jezik jer nema localStorage, pa bi
 * TransferState klijentu isporučio srpske kategorije čak i kad je EN aktivan.
 */
export const INITIAL_LANG = new InjectionToken<AppLang | null>('INITIAL_LANG');

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private translate = inject(TranslateService);
  private document = inject(DOCUMENT);
  private initialLang = inject(INITIAL_LANG, { optional: true });
  private _lang$ = new BehaviorSubject<AppLang>(DEFAULT_LANG);
  readonly lang$ = this._lang$.asObservable();

  init(): Promise<unknown> {
    this.translate.addLangs(SUPPORTED);
    this.translate.setDefaultLang(DEFAULT_LANG);
    const initial = this.initialLang ?? this.readStored() ?? DEFAULT_LANG;
    return this.setLang(initial);
  }

  getCurrentLang(): AppLang {
    return this._lang$.value;
  }

  setLang(lang: AppLang): Promise<unknown> {
    if (!SUPPORTED.includes(lang)) lang = DEFAULT_LANG;
    this._lang$.next(lang);
    this.persist(lang);
    if (this.document) {
      this.document.documentElement.lang = lang === 'sr' ? 'sr-Latn' : 'en';
    }
    return new Promise((resolve) => {
      this.translate.use(lang).subscribe({
        next: () => resolve(true),
        error: () => resolve(false),
      });
    });
  }

  private readStored(): AppLang | null {
    try {
      if (typeof localStorage !== 'undefined') {
        const v = localStorage.getItem(STORAGE_KEY);
        if (v === 'sr' || v === 'en') return v;
      }
    } catch { /* ignore */ }
    return readLangCookie(this.document);
  }

  private persist(lang: AppLang): void {
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, lang);
      }
    } catch { /* ignore */ }
    // Cookie je neophodan da SSR (Node, bez localStorage) zna jezik i pošalje
    // Accept-Language: en na /api/categories i sve ostale API pozive.
    try {
      if (this.document && typeof this.document.cookie === 'string') {
        this.document.cookie = `${STORAGE_KEY}=${lang}; path=/; max-age=${COOKIE_MAX_AGE}; SameSite=Lax`;
      }
    } catch { /* ignore */ }
  }
}

export function readLangCookie(doc: Document | null | undefined): AppLang | null {
  try {
    const raw = doc?.cookie ?? '';
    const match = raw.match(/(?:^|;\s*)rro_lang=(sr|en)(?:;|$)/);
    return match ? (match[1] as AppLang) : null;
  } catch {
    return null;
  }
}
