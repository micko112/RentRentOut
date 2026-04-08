import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

export type ConsentStatus = 'accepted' | 'declined' | null;

const GA_MEASUREMENT_ID = 'G-GYYJSDLKLB';
const STORAGE_KEY = 'cookie_consent';

@Injectable({ providedIn: 'root' })
export class CookieConsentService {

  /** null = korisnik još nije doneo odluku (prikazuje banner) */
  private statusSubject = new BehaviorSubject<ConsentStatus>(this.readFromStorage());
  status$ = this.statusSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: object) {
    if (isPlatformBrowser(this.platformId) && this.readFromStorage() === 'accepted') {
      this.loadGoogleAnalytics();
    }
  }

  get status(): ConsentStatus {
    return this.statusSubject.value;
  }

  accept(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(STORAGE_KEY, 'accepted');
      this.loadGoogleAnalytics();
    }
    this.statusSubject.next('accepted');
  }

  decline(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(STORAGE_KEY, 'declined');
    }
    this.statusSubject.next('declined');
  }

  private readFromStorage(): ConsentStatus {
    if (typeof localStorage === 'undefined') return null;
    const val = localStorage.getItem(STORAGE_KEY);
    if (val === 'accepted' || val === 'declined') return val;
    return null;
  }

  private loadGoogleAnalytics(): void {
    if (document.getElementById('ga-script')) return; // već učitan

    const script = document.createElement('script');
    script.id = 'ga-script';
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
    document.head.appendChild(script);

    const inline = document.createElement('script');
    inline.text = `
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());
      gtag('config', '${GA_MEASUREMENT_ID}');
    `;
    document.head.appendChild(inline);
  }
}
