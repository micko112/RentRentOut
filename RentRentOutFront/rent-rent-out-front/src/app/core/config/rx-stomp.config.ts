// src/app/core/config/rx-stomp.config.ts
import { RxStompConfig } from '@stomp/rx-stomp';
import { WS_BASE_URL } from './api.config';

export const myRxStompConfig: RxStompConfig = {
  // 1. Gde se kačimo?
  // Naša Java aplikacija sluša na /ws.
  // OBRATI PAŽNJU: Koristimo 'ws://' (ili 'wss://' za https) a ne 'http://'
  brokerURL: WS_BASE_URL,

  // 2. Koliko često proveravamo da li je veza živa (Heartbeat)
  // Pomaže da se veza ne prekine ako niko ne kuca ništa 20 sekundi
  heartbeatIncoming: 0,
  heartbeatOutgoing: 20000, // Šaljemo ping na svakih 20 sekundi

  // 3. Automatsko ponovno povezivanje ako server pukne pa se podigne
  reconnectDelay: 200,

};
