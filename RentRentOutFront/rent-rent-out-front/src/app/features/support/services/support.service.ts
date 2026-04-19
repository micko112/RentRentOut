import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';

export interface ChatMessage {
  role: 'user' | 'bot';
  text: string;
  timestamp: Date;
}

@Injectable({ providedIn: 'root' })
export class SupportService {
  private http = inject(HttpClient);
  private endpoint = `${API_BASE_URL}/support/ask`;

  messages: ChatMessage[] = [];

  ask(message: string): Observable<string> {
    const headers = new HttpHeaders({ 'X-Silent': 'true' });
    return this.http
      .post<{ reply: string }>(this.endpoint, { message }, { headers })
      .pipe(map(res => res.reply));
  }

  addMessage(role: 'user' | 'bot', text: string): void {
    this.messages.push({ role, text, timestamp: new Date() });
  }

  clear(): void {
    this.messages = [];
  }
}
