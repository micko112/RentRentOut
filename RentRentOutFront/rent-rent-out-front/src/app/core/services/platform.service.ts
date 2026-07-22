import { Injectable } from '@angular/core';

const ACCESS_KEY  = 'rro_access_token';
const REFRESH_KEY = 'rro_refresh_token';

@Injectable({ providedIn: 'root' })
export class PlatformService {
  private _isNative: boolean | null = null;
  private _cachedAccess: string | null = null;

  get isNative(): boolean {
    if (this._isNative !== null) return this._isNative;
    if (typeof window === 'undefined') {
      this._isNative = false;
      return false;
    }
    try {
      const cap = (window as any).Capacitor;
      this._isNative = !!(cap && typeof cap.isNativePlatform === 'function' && cap.isNativePlatform());
    } catch {
      this._isNative = false;
    }
    return this._isNative;
  }

  /**
   * Pre-loaduje access token iz Preferences u in-memory cache.
   * Zove se preko APP_INITIALIZER-a pre bilo kog HTTP poziva.
   */
  async hydrate(): Promise<void> {
    if (!this.isNative) return;
    const token = await this.readAccessToken();
    if (token) this._cachedAccess = token;
  }

  private async readAccessToken(): Promise<string | null> {
    try {
      const { Preferences } = await import('@capacitor/preferences');
      const { value } = await Preferences.get({ key: ACCESS_KEY });
      return value ?? null;
    } catch {
      return null;
    }
  }

  async getRefreshToken(): Promise<string | null> {
    if (!this.isNative) return null;
    try {
      const { Preferences } = await import('@capacitor/preferences');
      const { value } = await Preferences.get({ key: REFRESH_KEY });
      return value ?? null;
    } catch {
      return null;
    }
  }

  /** Sinhrono postavlja cache, asinhrono peristira u Preferences. */
  saveTokens(accessToken: string, refreshToken: string): void {
    this._cachedAccess = accessToken;
    if (!this.isNative) return;
    import('@capacitor/preferences').then(({ Preferences }) => {
      Preferences.set({ key: ACCESS_KEY,  value: accessToken }).catch(() => {});
      Preferences.set({ key: REFRESH_KEY, value: refreshToken }).catch(() => {});
    }).catch(() => {});
  }

  clearTokens(): void {
    this._cachedAccess = null;
    if (!this.isNative) return;
    import('@capacitor/preferences').then(({ Preferences }) => {
      Preferences.remove({ key: ACCESS_KEY }).catch(() => {});
      Preferences.remove({ key: REFRESH_KEY }).catch(() => {});
    }).catch(() => {});
  }

  getCachedAccess(): string | null { return this._cachedAccess; }
}
