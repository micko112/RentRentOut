import {Component, OnInit, ViewChild, ElementRef, AfterViewChecked, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';
import {Message, MessageGroup} from '../../../../shared/models/message.model';
import {ConversationPreview} from '../../../../shared/models/conversation-preview.model';
import {Subscription, interval} from 'rxjs';
import {WebsocketService} from '../../../../core/services/websocket.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, InitialsPipe, RouterLink],
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.css']
})
export class InboxComponent implements OnInit, AfterViewChecked, OnDestroy {

  // Left side
  conversations: ConversationPreview[] =[];
  activeConversation: ConversationPreview | null = null;

  // Right side
  groupedMessages: MessageGroup[] =[];

  messages: Message[] =[];
  newMessageContent: string = '';
  myUserId: number | null = null;

  private messageSub!: Subscription;
  private pollSub!: Subscription;
  private conversationsLoaded = false;
  private pendingChatCheck = false;

  // Auto-scroll to bottom
  @ViewChild('chatScroll') private chatScrollContainer!: ElementRef;
  private scrollToBottomNeeded = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private websocketService: WebsocketService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.myUserId = user.id;
    }

    this.authService.currentUser$.subscribe(u => {
      if (u && u.id) {
        this.myUserId = u.id;
      }
    });

    this.loadConversations();
    this.route.queryParamMap.subscribe(() => {
      if (this.conversationsLoaded) {
        this.checkForNewChatRequest();
      } else {
        this.pendingChatCheck = true;
      }
    });

    this.startPolling();

    this.websocketService.connect();
    this.messageSub = this.websocketService.watch('/user/queue/messages').subscribe((stompMsg) => {
      const incomingMessage: Message = JSON.parse(stompMsg.body);
      this.handleIncomingMessage(incomingMessage);
    });
  }

  handleIncomingMessage(msg: Message) {
      console.log('Stigla live poruka!', msg);

      if (!this.myUserId) {
        const u = this.authService.currentUserValue;
        if (u && u.id) {
          this.myUserId = u.id;
        }
      }

      // Scenario A: fake room (ID 0) and we sent the first message
      if (this.activeConversation && this.activeConversation.id === 0 && msg.senderId === this.myUserId) {
        // Backend created a real room. Update the id.
        this.activeConversation.id = msg.conversationId;
        this.messages = this.messages.map(m =>
          m.conversationId === 0 ? { ...m, conversationId: msg.conversationId } : m
        );
        this.removeOptimisticDuplicate(msg);
        this.messages.push(msg);
        this.updateGroupedMessages();
        this.scrollToBottomNeeded = true;
      }
      // Scenario B: normal room, message belongs to active room
      else if (this.activeConversation && this.activeConversation.id === msg.conversationId) {
        this.removeOptimisticDuplicate(msg);
        this.messages.push(msg);
        this.scrollToBottomNeeded = true;
      }

      // Refresh left list so chat jumps to top
      this.loadConversations();

      // Force UI update for new bubbles
      this.cdr.detectChanges();
    }

  loadConversations() {
    this.chatService.getMyConversations().subscribe(res => {
      this.conversations = res.content;
      this.conversationsLoaded = true;
      if (this.pendingChatCheck) {
        this.pendingChatCheck = false;
        this.checkForNewChatRequest();
      }
      /*
      if (this.conversations.length > 0 && !this.activeConversation) {
        this.openConversation(this.conversations[0]);
      }
      */
    });
  }

  openConversation(conv: ConversationPreview) {
    this.activeConversation = conv;

    if (conv.id === 0) {
      this.messages = [];
      this.scrollToBottomNeeded = true;
      return;
    }

    this.chatService.getMessages(conv.id).subscribe(res => {
      this.messages = res.content;
      this.updateGroupedMessages();
      this.scrollToBottomNeeded = true;
    });
  }

  sendMessage() {
    if (!this.newMessageContent.trim() || !this.activeConversation) return;

    const content = this.newMessageContent.trim();
    const request = {
      adId: this.activeConversation.adId,
      receiverId: this.activeConversation.otherParticipant.id,
      content
    };

    this.newMessageContent = '';

    this.appendOptimisticMessage(content);
    // Send via WebSocket
    this.websocketService.sendMessage('/app/chat.send', request);
  }

  ngAfterViewChecked() {
    if (this.scrollToBottomNeeded) {
      this.scrollToBottom();
      this.scrollToBottomNeeded = false;
    }
  }

  private scrollToBottom(): void {
    try {
      this.chatScrollContainer.nativeElement.scrollTop = this.chatScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  ngOnDestroy(): void {
    if (this.messageSub) {
      this.messageSub.unsubscribe();
    }
    if (this.pollSub) {
      this.pollSub.unsubscribe();
    }
  }

  checkForNewChatRequest() {
    const adId = Number(this.route.snapshot.queryParamMap.get('newChatAdId'));
    const receiverId = Number(this.route.snapshot.queryParamMap.get('receiverId'));
    const adTitle = this.route.snapshot.queryParamMap.get('adTitle');
    const receiverName = this.route.snapshot.queryParamMap.get('receiverName');

    if (adId && receiverId) {
      const existingConv = this.conversations.find(c => c.adId === adId && c.otherParticipant.id === receiverId);

      if (existingConv) {
        this.openConversation(existingConv);
      } else {
        const existingTemp = this.conversations.find(
          c => c.id === 0 && c.adId === adId && c.otherParticipant.id === receiverId
        );

        if (existingTemp) {
          this.activeConversation = existingTemp;
          this.messages = [];
        } else {
          const tempConv: ConversationPreview = {
            id: 0,
            adId: adId,
            adTitle: adTitle || 'Nepoznat oglas',
            adThumbnail: 'assets/images/placeholder.png',
            lastMessagePreview: 'Zapocnite razgovor...',
            updatedAt: new Date().toISOString(),
            unreadCount: 0,
            otherParticipant: {
              id: receiverId,
              displayName: receiverName || 'Korisnik',
              identified: false,
              avatarUrl: '',
              locationDisplay: '',
              phoneNumber: '',
              positiveReviews: 0,
              negativeReviews: 0,
              createdAt: ''
            }
          };

          this.conversations.unshift(tempConv);
          this.activeConversation = tempConv;
          this.messages = [];
        }
      }

      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {
          newChatAdId: null,
          receiverId: null,
          adTitle: null,
          receiverName: null
        },
        queryParamsHandling: 'merge'
      });
    }
  }

  private appendOptimisticMessage(content: string): void {
    if (!this.activeConversation) return;
    const tempMsg: Message = {
      id: -Date.now(),
      conversationId: this.activeConversation.id,
      senderId: this.myUserId || 0,
      content,
      read: true,
      createdAt: new Date().toISOString()
    };
    (tempMsg as any)._temp = true;
    this.messages.push(tempMsg);
    this.updateGroupedMessages();
    this.scrollToBottomNeeded = true;
  }

  private removeOptimisticDuplicate(msg: Message): void {
    const idx = this.messages.findIndex(m =>
      (m as any)._temp &&
      m.senderId === msg.senderId &&
      m.content === msg.content &&
      (m.conversationId === msg.conversationId || m.conversationId === 0)
    );
    if (idx >= 0) {
      this.messages.splice(idx, 1);
    }
  }

  private startPolling(): void {
    this.pollSub = interval(5000).subscribe(() => {
      if (!this.websocketService.isConnected()) {
        this.websocketService.connect();
      }
      this.loadConversations();
      this.refreshActiveMessages();
    });
  }

  private refreshActiveMessages(): void {
    if (!this.activeConversation || this.activeConversation.id === 0) return;
    if (this.messages.some(m => (m as any)._temp)) return;
    this.chatService.getMessages(this.activeConversation.id).subscribe(res => {
      this.messages = res.content;
      this.scrollToBottomNeeded = true;
    });
  }
  updateGroupedMessages(): void {
    if (!this.messages || this.messages.length === 0) {
      this.groupedMessages = [];
      return;
    }

    const groupsMap = new Map<string, Message[]>();
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    this.messages.forEach(msg => {
      const msgDate = new Date(msg.createdAt);
      let dateLabel = '';

      // Proveravamo da li je "Danas", "Juče" ili stariji datum
      if (this.isSameDay(msgDate, today)) {
        dateLabel = 'Danas';
      } else if (this.isSameDay(msgDate, yesterday)) {
        dateLabel = 'Juče';
      } else {
        // Ako je starije, formatiramo kao npr. "15.03.2026."
        // (Najlakše ugrađenim JS metodama da ne vučemo DatePipe u TS ako ne moramo)
        const d = msgDate.getDate().toString().padStart(2, '0');
        const m = (msgDate.getMonth() + 1).toString().padStart(2, '0');
        const y = msgDate.getFullYear();
        dateLabel = `${d}.${m}.${y}.`;
      }

      // Ako grupa za ovaj datum već postoji, dodaj poruku u nju
      if (groupsMap.has(dateLabel)) {
        groupsMap.get(dateLabel)!.push(msg);
      } else {
        // Ako ne postoji, napravi novu grupu sa ovom prvom porukom
        groupsMap.set(dateLabel,[msg]);
      }
    });

    // Pretvaramo Map objekat nazad u niz naših MessageGroup interfejsa
    this.groupedMessages = Array.from(groupsMap, ([dateLabel, messages]) => ({
      dateLabel,
      messages
    }));
  }

  // Pomoćna funkcija za poređenje datuma (bez vremena)
  private isSameDay(d1: Date, d2: Date): boolean {
    return d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate();
  }
}
