import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';

export interface ChatMessage {
  role: 'user' | 'bot';
  text: string;
  timestamp: Date;
}

export interface StreamHandlers {
  onToken: (token: string) => void;
  onDone: () => void;
  onError: (err: unknown) => void;
}

@Injectable({ providedIn: 'root' })
export class SupportService {
  private http = inject(HttpClient);
  private endpoint = `${API_BASE_URL}/support/ask`;
  private streamEndpoint = `${API_BASE_URL}/support/ask/stream`;

  messages: ChatMessage[] = [];

  ask(message: string): Observable<string> {
    const headers = new HttpHeaders({ 'X-Silent': 'true' });
    return this.http
      .post<{ reply: string }>(this.endpoint, { message }, { headers })
      .pipe(map(res => res.reply));
  }

  async askStream(message: string, handlers: StreamHandlers): Promise<() => void> {
    const controller = new AbortController();
    (async () => {
      try {
        const res = await fetch(this.streamEndpoint, {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'text/event-stream',
            'X-Silent': 'true',
          },
          body: JSON.stringify({ message }),
          signal: controller.signal,
        });

        if (!res.ok || !res.body) {
          handlers.onError(new Error(`HTTP ${res.status}`));
          return;
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        let currentEvent = 'message';

        while (true) {
          const { value, done } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });

          let idx: number;
          while ((idx = buffer.indexOf('\n\n')) !== -1) {
            const rawFrame = buffer.slice(0, idx);
            buffer = buffer.slice(idx + 2);

            currentEvent = 'message';
            const dataLines: string[] = [];
            for (const line of rawFrame.split('\n')) {
              if (line.startsWith('event:')) {
                currentEvent = line.slice(6).trim();
              } else if (line.startsWith('data:')) {
                dataLines.push(line.slice(5).replace(/^ /, ''));
              }
            }
            const data = dataLines.join('\n');
            if (currentEvent === 'done') {
              handlers.onDone();
              return;
            }
            if (currentEvent === 'error') {
              handlers.onError(new Error(data));
              return;
            }
            handlers.onToken(data.replace(/\\n/g, '\n'));
          }
        }
        handlers.onDone();
      } catch (err) {
        if ((err as Error).name !== 'AbortError') {
          handlers.onError(err);
        }
      }
    })();
    return () => controller.abort();
  }

  addMessage(role: 'user' | 'bot', text: string): void {
    this.messages.push({ role, text, timestamp: new Date() });
  }

  updateLastBotMessage(text: string): void {
    for (let i = this.messages.length - 1; i >= 0; i--) {
      if (this.messages[i].role === 'bot') {
        this.messages[i].text = text;
        return;
      }
    }
  }

  clear(): void {
    this.messages = [];
  }
}
