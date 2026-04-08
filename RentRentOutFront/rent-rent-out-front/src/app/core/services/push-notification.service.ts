import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL, VAPID_PUBLIC_KEY } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class PushNotificationService {

  constructor(private http: HttpClient) {}

  async requestAndSubscribe(): Promise<void> {
    if (typeof navigator === 'undefined' || typeof window === 'undefined') {
      return;
    }
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      return;
    }

    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      return;
    }

    const registration = await navigator.serviceWorker.ready;
    const existing = await registration.pushManager.getSubscription();
    if (existing) {
      return; // already subscribed
    }

    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: this.urlBase64ToUint8Array(VAPID_PUBLIC_KEY)
    });

    const json = subscription.toJSON();
    this.http.post(`${API_BASE_URL}/push/subscribe`, {
      endpoint: json.endpoint,
      p256dh: json.keys?.['p256dh'],
      auth: json.keys?.['auth']
    }).subscribe({
      error: (err) => console.error('Failed to save push subscription:', err)
    });
  }

  async unsubscribe(): Promise<void> {
    if (typeof navigator === 'undefined') return;
    if (!('serviceWorker' in navigator)) return;

    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();
    if (!subscription) return;

    const json = subscription.toJSON();
    this.http.delete(`${API_BASE_URL}/push/unsubscribe`, {
      body: { endpoint: json.endpoint }
    }).subscribe({
      error: (err) => console.error('Failed to remove push subscription:', err)
    });

    await subscription.unsubscribe();
  }

  private urlBase64ToUint8Array(base64String: string): Uint8Array {
    if (typeof window === 'undefined') return new Uint8Array(0);
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
  }
}
