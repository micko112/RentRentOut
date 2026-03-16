
import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { myRxStompConfig } from '../config/rx-stomp.config';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {

  // Instanca RxStomp klijenta
  public rxStomp: RxStomp;

  constructor() {
    this.rxStomp = new RxStomp();
  }

  public connect() {
    // 1. Čitamo najsvežiji token iz browsera BAŠ U TRENUTKU konekcije
    const token = localStorage.getItem('authToken');

    // 2. Konfigurišemo STOMP
    this.rxStomp.configure({
      brokerURL: 'ws://localhost:8080/ws', // Uklonili smo .withSockJS() na backendu pa gadjamo /ws
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
      reconnectDelay: 2000,
      // 3. OVO JE KLJUČ: Ubacujemo svež token u heder
      connectHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    // 4. Palimo cev!
    this.rxStomp.activate();
  }

  // Metoda kojom šaljemo poruku na server (npr. na '/app/chat.send')
  public sendMessage(destination: string, body: any) {
    this.rxStomp.publish({
      destination: destination,
      body: JSON.stringify(body) // STOMP šalje stringove, pa JSON moramo da pretvorimo u tekst
    });
  }

  // Metoda kojom "slušamo" kanal (npr. '/user/queue/messages')
  public watch(destination: string) {
    return this.rxStomp.watch(destination);
  }

  // Kada se izlogujemo, gasimo cev
  public deactivate() {
    this.rxStomp.deactivate();
  }
}
