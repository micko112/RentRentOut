import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { WS_BASE_URL } from '../config/api.config';
import { AuthService } from '../../features/auth/services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {

  public rxStomp: RxStomp;
  private lastToken: string | null = null;

  constructor(private authService: AuthService) {
    this.rxStomp = new RxStomp();
  }

  public connect() {
    const token = this.authService.wsToken;
    if (!token) {
      return;
    }
    if (this.rxStomp.connected() && this.lastToken === token) {
      return;
    }
    this.lastToken = token;

    const start = () => {
      // 2. Konfigurisi STOMP
      this.rxStomp.configure({
        brokerURL: WS_BASE_URL,
        heartbeatIncoming: 0,
        heartbeatOutgoing: 20000,
        reconnectDelay: 2000,
        connectHeaders: {
          Authorization: `Bearer ${token}`
        }
      });

      // 3. Palimo cev
      this.rxStomp.activate();
    };

    if (this.rxStomp.active) {
      this.rxStomp.deactivate().then(() => start());
    } else {
      start();
    }
  }

  // Metoda kojom saljemo poruku na server (npr. na '/app/chat.send')
  public sendMessage(destination: string, body: any) {
    this.rxStomp.publish({
      destination: destination,
      body: JSON.stringify(body)
    });
  }

  // Metoda kojom "slusamo" kanal (npr. '/user/queue/messages')
  public watch(destination: string) {
    return this.rxStomp.watch(destination);
  }

  public isConnected(): boolean {
    return this.rxStomp.connected();
  }

  // Kada se izlogujemo, gasimo cev
  public deactivate() {
    this.rxStomp.deactivate();
  }
}
