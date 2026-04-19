import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class CapacitorAppService {
  private router = inject(Router);
  private location = inject(Location);

  private isNative = false;

  async initialize(): Promise<void> {
    try {
      const { Capacitor } = await import('@capacitor/core');
      this.isNative = Capacitor.isNativePlatform();
    } catch {
      return;
    }

    if (!this.isNative) return;

    await this.setupStatusBar();
    await this.setupSplashScreen();
    await this.setupBackButton();
    await this.setupKeyboard();
  }

  private async setupStatusBar(): Promise<void> {
    try {
      const { StatusBar, Style } = await import('@capacitor/status-bar');
      await StatusBar.setStyle({ style: Style.Light });
      await StatusBar.setBackgroundColor({ color: '#813181' });
    } catch {}
  }

  private async setupSplashScreen(): Promise<void> {
    try {
      const { SplashScreen } = await import('@capacitor/splash-screen');
      await SplashScreen.hide({ fadeOutDuration: 300 });
    } catch {}
  }

  private async setupBackButton(): Promise<void> {
    try {
      const { App } = await import('@capacitor/app');
      App.addListener('backButton', ({ canGoBack }) => {
        const currentUrl = this.router.url;
        const isRoot = currentUrl === '/' || currentUrl === '/ads' || currentUrl === '';

        if (isRoot || !canGoBack) {
          App.minimizeApp();
        } else {
          this.location.back();
        }
      });
    } catch {}
  }

  private async setupKeyboard(): Promise<void> {
    try {
      const { Keyboard } = await import('@capacitor/keyboard');
      Keyboard.addListener('keyboardWillShow', info => {
        document.body.style.paddingBottom = `${info.keyboardHeight}px`;
      });
      Keyboard.addListener('keyboardWillHide', () => {
        document.body.style.paddingBottom = '';
      });
    } catch {}
  }

  get native(): boolean {
    return this.isNative;
  }
}
