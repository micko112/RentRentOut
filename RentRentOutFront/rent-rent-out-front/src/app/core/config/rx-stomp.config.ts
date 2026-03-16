// src/app/core/config/rx-stomp.config.ts
import { RxStompConfig } from '@stomp/rx-stomp';

export const myRxStompConfig: RxStompConfig = {
  // 1. Gde se kačimo?
  // Naša Java aplikacija sluša na /ws.
  // OBRATI PAŽNJU: Koristimo 'ws://' (ili 'wss://' za https) a ne 'http://'
  brokerURL: 'ws://localhost:8080/ws',

  // 2. Koliko često proveravamo da li je veza živa (Heartbeat)
  // Pomaže da se veza ne prekine ako niko ne kuca ništa 20 sekundi
  heartbeatIncoming: 0,
  heartbeatOutgoing: 20000, // Šaljemo ping na svakih 20 sekundi

  // 3. Automatsko ponovno povezivanje ako server pukne pa se podigne
  reconnectDelay: 200,

  // 4. KLJUČNO: OVO JE NAŠ TOKEN!
  // beforeConnect se izvršava mili-sekundu pre nego što Angular otvori cev.
  // Ovde dinamički vadimo token iz localStorage-a.
  beforeConnect: (client: any) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      // Ubacujemo token u STOMP heder koji Java očekuje
      client.connectHeaders = {
        Authorization: `Bearer ${token}`
      };
    }
  }
};
