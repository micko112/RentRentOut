import { Injectable, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

export type AppLang = 'sr' | 'en';

const STORAGE_KEY = 'rro_lang';
const SUPPORTED: AppLang[] = ['sr', 'en'];
const DEFAULT_LANG: AppLang = 'sr';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private translate = inject(TranslateService);
  private _lang$ = new BehaviorSubject<AppLang>(DEFAULT_LANG);
  readonly lang$ = this._lang$.asObservable();

  init(): Promise<unknown> {
    this.translate.addLangs(SUPPORTED);
    this.translate.setDefaultLang(DEFAULT_LANG);
    const initial = this.readStored() ?? DEFAULT_LANG;
    return this.setLang(initial);
  }

  getCurrentLang(): AppLang {
    return this._lang$.value;
  }

  setLang(lang: AppLang): Promise<unknown> {
    if (!SUPPORTED.includes(lang)) lang = DEFAULT_LANG;
    this._lang$.next(lang);
    this.persist(lang);
    if (typeof document !== 'undefined') {
      document.documentElement.lang = lang === 'sr' ? 'sr-Latn' : 'en';
    }
    return new Promise((resolve) => {
      this.translate.use(lang).subscribe({
        next: () => resolve(true),
        error: () => resolve(false),
      });
    });
  }

  private readStored(): AppLang | null {
    if (typeof localStorage === 'undefined') return null;
    try {
      const v = localStorage.getItem(STORAGE_KEY);
      return v === 'sr' || v === 'en' ? v : null;
    } catch {
      return null;
    }
  }

  private persist(lang: AppLang): void {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(STORAGE_KEY, lang);
    } catch {
      // ignore
    }
  }
}
