import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../config/api.config';
import { PlatformService } from './platform.service';

@Injectable({ providedIn: 'root' })
export class MobilePushService {
  private http = inject(HttpClient);
  private platform = inject(PlatformService);
  private initialized = false;

  async initialize(): Promise<void> {
    if (this.initialized) return;
    if (!this.platform.isNative) return;

    try {
      const { PushNotifications } = await import('@capacitor/push-notifications');

      const perm = await PushNotifications.checkPermissions();
      let status = perm.receive;
      if (status === 'prompt' || status === 'prompt-with-rationale') {
        status = (await PushNotifications.requestPermissions()).receive;
      }
      if (status !== 'granted') return;

      await PushNotifications.register();

      PushNotifications.addListener('registration', token => {
        this.registerTokenWithBackend(token.value);
      });

      PushNotifications.addListener('registrationError', err => {
        console.error('Push registration error', err);
      });

      PushNotifications.addListener('pushNotificationReceived', notification => {
        // TODO: prikaz in-app toast/badge update
        console.log('Push received', notification);
      });

      PushNotifications.addListener('pushNotificationActionPerformed', action => {
        // TODO: deep link u aplikaciju na osnovu action.notification.data
        console.log('Push tapped', action);
      });

      this.initialized = true;
    } catch (err) {
      console.error('MobilePushService init failed', err);
    }
  }

  private registerTokenWithBackend(fcmToken: string): void {
    this.http.post(`${API_BASE_URL}/push/mobile-register`, {
      token: fcmToken,
      platform: 'android',
    }).subscribe({
      error: err => console.error('Failed to register FCM token', err),
    });
  }
}
