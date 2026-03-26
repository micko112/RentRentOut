import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ConsentStatus = 'accepted' | 'declined' | null;

// ─── Zameni sa tvojim GA4 Measurement ID ─────────────────────────────────────
// Nađi ga na: analytics.google.com → Admin → Data Streams → tvoj stream → Measurement ID
const GA_MEASUREMENT_ID = 'G-GYYJSDLKLB';
// ─────────────────────────────────────────────────────────────────────────────

const STORAGE_KEY = 'cookie_consent';

@Injectable({ providedIn: 'root' })
export class CookieConsentService {

  /** null = korisnik još nije doneo odluku (prikazuje banner) */
  private statusSubject = new BehaviorSubject<ConsentStatus>(this.readFromStorage());
  status$ = this.statusSubject.asObservable();

  constructor() {
    // Ako je prethodno prihvatio, učitaj GA odmah
    if (this.readFromStorage() === 'accepted') {
      this.loadGoogleAnalytics();
    }
  }

  get status(): ConsentStatus {
    return this.statusSubject.value;
  }

  accept(): void {
    localStorage.setItem(STORAGE_KEY, 'accepted');
    this.statusSubject.next('accepted');
    this.loadGoogleAnalytics();
  }

  decline(): void {
    localStorage.setItem(STORAGE_KEY, 'declined');
    this.statusSubject.next('declined');
  }

  private readFromStorage(): ConsentStatus {
    const val = localStorage.getItem(STORAGE_KEY);
    if (val === 'accepted' || val === 'declined') return val;
    return null;
  }

  private loadGoogleAnalytics(): void {
    if (GA_MEASUREMENT_ID === 'G-GYYJSDLKLB') return; // placeholder — ne učitavaj

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
