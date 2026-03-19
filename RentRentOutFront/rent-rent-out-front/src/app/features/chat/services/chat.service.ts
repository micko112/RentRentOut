import { Injectable } from '@angular/core';
import {Message} from '../../../shared/models/message.model';
import {HttpClient, HttpParams} from '@angular/common/http';
import {API_BASE_URL} from '../../../core/config/api.config';
import {Observable} from 'rxjs';
import {Page} from '../../../shared/models/adPreview.model';
import {ConversationPreview} from '../../../shared/models/conversation-preview.model';
import {SendMessageRequest} from '../../../shared/models/send-message-request.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  constructor(private http: HttpClient) { }
  private url = `${API_BASE_URL}/chat`;

  getMyConversations(page: number=0, size: number=20): Observable<Page<ConversationPreview>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ConversationPreview>>(`${this.url}/conversations`, {params: params});
  }
  getMessages(conversationId: number, page: number = 0, size: number = 50):  Observable<Page<Message>> {
    let params = new HttpParams().set('conversationId', conversationId).set('page', page).set('size', size);
    return this.http.get<Page<Message>>(`${this.url}/conversations/${conversationId}/messages`, {params: params});
  }
  sendMessage(request: SendMessageRequest): Observable<Message> {
    return this.http.post<Message>(`${this.url}/send`, request);
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.url}/unread-count`);
  }
}
