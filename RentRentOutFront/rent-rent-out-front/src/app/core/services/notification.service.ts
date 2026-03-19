import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ConversationPreview } from '../../shared/models/conversation-preview.model';
import { ChatService } from '../../features/chat/services/chat.service';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private totalUnreadSubject = new BehaviorSubject<number>(0);
  totalUnread$ = this.totalUnreadSubject.asObservable();

  constructor(private chatService: ChatService) {}

  /** Povuci tačan count direktno iz baze (poziva se pri inicijalizaciji Sidebara). */
  initialize(): void {
    this.chatService.getUnreadCount().subscribe({
      next: (count) => this.totalUnreadSubject.next(count),
      error: () => { /* korisnik nije ulogovan ili mrežna greška — ostaje 0 */ }
    });
  }

  /** Ažurira count na osnovu sume iz liste konverzacija (poziva se u Inbox-u). */
  updateFromConversations(conversations: ConversationPreview[]): void {
    const total = conversations.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
    this.totalUnreadSubject.next(total);
  }

  /** Oduzmi kada korisnik otvori konverzaciju (optimistično, bez HTTP poziva). */
  onConversationOpened(unreadCount: number): void {
    const current = this.totalUnreadSubject.value;
    this.totalUnreadSubject.next(Math.max(0, current - unreadCount));
  }

  /** Uvećaj za 1 kada poruka stigne u pozadinsku konverzaciju (WebSocket). */
  onNewMessageInBackground(): void {
    this.totalUnreadSubject.next(this.totalUnreadSubject.value + 1);
  }
}
