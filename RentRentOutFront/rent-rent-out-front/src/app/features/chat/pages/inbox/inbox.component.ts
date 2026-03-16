import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';
import {Message} from '../../../../shared/models/message.model';
import {ConversationPreview} from '../../../../shared/models/conversation-preview.model';
import {Subscription} from 'rxjs';
import {WebsocketService} from '../../../../core/services/websocket.service';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, InitialsPipe],
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.css']
})
export class InboxComponent implements OnInit, AfterViewChecked {

  // Leva strana
  conversations: ConversationPreview[] =[];
  activeConversation: ConversationPreview | null = null;

  // Desna strana
  messages: Message[] =[];
  newMessageContent: string = '';
  myUserId: number | null = null;

  private messageSub!: Subscription;

  // Za auto-skrol na dno chata
  @ViewChild('chatScroll') private chatScrollContainer!: ElementRef;
  private scrollToBottomNeeded = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private websocketService: WebsocketService
  ) {}

  ngOnInit(): void {

    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.myUserId = user.id;
    }

    this.loadConversations();
    this.websocketService.connect();
    this.messageSub = this.websocketService.watch('/user/queue/messages').subscribe((stompMsg) => {
      // Poruka stiže kao običan tekst, moramo da je pretvorimo u JSON (naš Message objekt)
      const incomingMessage: Message = JSON.parse(stompMsg.body);

      this.handleIncomingMessage(incomingMessage);
    });
  }
  handleIncomingMessage(msg: Message) {
    console.log("Stigla live poruka!", msg);

    // 1. Da li je otvorena soba u kojoj je stigla poruka?
    if (this.activeConversation && this.activeConversation.id === msg.conversationId) {
      // Ako jeste, samo je gurnemo u niz i Angular je iste sekunde crta na ekranu!
      this.messages.push(msg);
      this.scrollToBottomNeeded = true;
    }

    // 2. Bez obzira gde smo, osvežavamo levu listu soba
    // (Ovo će pomeriti sobu na vrh i pokazati 'lastMessagePreview')
    this.loadConversations();
  }
  loadConversations() {
    this.chatService.getMyConversations().subscribe(res => {
      this.conversations = res.content;
      // Ako nam treba da odma otvorimo prvi chat u listi
      /*
      if (this.conversations.length > 0 && !this.activeConversation) {
        this.openConversation(this.conversations[0]);
      }
      */
    });
  }

  openConversation(conv: ConversationPreview) {
    this.activeConversation = conv;

    this.chatService.getMessages(conv.id).subscribe(res => {
      // Backend vraća ASC (najstarije prve), pa je to odlično za iscrtavanje od gore na dole
      this.messages = res.content;
      this.scrollToBottomNeeded = true; //  da skroluje na novu poruku
    });
  }

  sendMessage() {
    // Sprečavamo slanje praznih poruka
    if (!this.newMessageContent.trim() || !this.activeConversation) return;

    // Pakujemo podatke u DTO koji Java očekuje
    const request = {
      adId: this.activeConversation.adId,
      receiverId: this.activeConversation.otherParticipant.id,
      content: this.newMessageContent.trim()
    };

    // 1. Čistimo input polje odmah da bi korisnik imao osećaj brzine
    this.newMessageContent = '';

    // 2. ŠALJEMO KROZ CEV (WebSocket)
    // Gađamo destinaciju koju smo definisali u WebSocketConfig.java (@MessageMapping("/chat.send"))
    this.websocketService.sendMessage('/app/chat.send', request);

    // NAPOMENA: Ne dodajemo poruku u niz `this.messages` ovde!
    // Zašto? Zato što je naš Spring Boot backend pametan. Kada primi poruku na /app/chat.send,
    // on je sačuva u bazu i odmah je vrati nazad obojici (i tebi i sagovorniku) kroz /user/queue/messages.
    // Tvoja metoda `handleIncomingMessage` će uhvatiti taj povratni odgovor i iscrtati ga na ekranu.
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
    // Važno za performanse: Kad korisnik pređe na neku drugu stranicu (npr. Profil),
    // prestajemo da slušamo poruke da ne bismo trošili memoriju browsera.
    if (this.messageSub) {
      this.messageSub.unsubscribe();
    }
  }
}
