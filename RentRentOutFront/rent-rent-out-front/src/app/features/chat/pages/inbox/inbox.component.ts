import {Component, OnInit, ViewChild, ElementRef, AfterViewChecked, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';
import {Message} from '../../../../shared/models/message.model';
import {ConversationPreview} from '../../../../shared/models/conversation-preview.model';
import {Subscription} from 'rxjs';
import {WebsocketService} from '../../../../core/services/websocket.service';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, InitialsPipe],
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.css']
})
export class InboxComponent implements OnInit, AfterViewChecked, OnDestroy {

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

      // SCENARIO A: Mi smo u "lažnoj" sobi (ID: 0) i mi smo upravo poslali prvu poruku!
      if (this.activeConversation && this.activeConversation.id === 0 && msg.senderId === this.myUserId) {
        // Backend je napravio pravu sobu. Ažuriramo naš lažni ID na onaj pravi iz baze!
        this.activeConversation.id = msg.conversationId;
        this.messages.push(msg);
        this.scrollToBottomNeeded = true;
      }
      // SCENARIO B: Mi smo u normalnoj, postojećoj sobi i poruka je za tu sobu
      else if (this.activeConversation && this.activeConversation.id === msg.conversationId) {
        this.messages.push(msg);
        this.scrollToBottomNeeded = true;
      }

      // U svakom slučaju, osveži levu listu soba da bi ovaj chat skočio na vrh
      this.loadConversations();

      // MAGIJA: Drmamo Angular da OBAVEZNO ODMAH nacrta nove oblačiće na ekranu!
      this.cdr.detectChanges();
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
  checkForNewChatRequest() {
    // Čitamo parametre iz URL-a
    const adId = Number(this.route.snapshot.queryParamMap.get('newChatAdId'));
    const receiverId = Number(this.route.snapshot.queryParamMap.get('receiverId'));
    const adTitle = this.route.snapshot.queryParamMap.get('adTitle');
    const receiverName = this.route.snapshot.queryParamMap.get('receiverName');

    if (adId && receiverId) {
      // 1. Proveravamo da li već imamo sobu sa ovim čovekom za ovaj oglas
      const existingConv = this.conversations.find(c => c.adId === adId && c.otherParticipant.id === receiverId);

      if (existingConv) {
        // Soba postoji! Samo je otvori.
        this.openConversation(existingConv);
      } else {
        // 2. Soba NE POSTOJI. Pravimo "Lažnu" sobu na ekranu da bi korisnik mogao da kuca.
        const tempConv: ConversationPreview = {
          id: 0, // 0 znači da još nije u bazi
          adId: adId,
          adTitle: adTitle || 'Nepoznat oglas',
          adThumbnail: 'assets/images/placeholder.png', // Stavljamo placeholder
          lastMessagePreview: 'Započnite razgovor...',
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

        // Gurnemo ovu privremenu sobu na vrh liste (levo) da je korisnik vidi
        this.conversations.unshift(tempConv);

        // Postavljamo je kao aktivnu i praznimo poruke (desno)
        this.activeConversation = tempConv;
        this.messages =[];
      }

      // 3. ZAVRŠENO: Očisti URL da se ne bi opet otvaralo pri refreshu (F5)
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {
          newChatAdId: null,
          receiverId: null,
          adTitle: null,
          receiverName: null
        },
        queryParamsHandling: 'merge' // Briše ove parametre, ostavlja sve drugo ako ima
      });
    }
  }
}
