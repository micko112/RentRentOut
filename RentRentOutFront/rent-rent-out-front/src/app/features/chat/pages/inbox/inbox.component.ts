import {Component, OnInit, ViewChild, ElementRef, AfterViewChecked, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';
import {Message, MessageGroup} from '../../../../shared/models/message.model';
import {ConversationPreview} from '../../../../shared/models/conversation-preview.model';
import {Subject, Subscription, interval, forkJoin, of} from 'rxjs';
import {takeUntil, exhaustMap, catchError} from 'rxjs/operators';
import {WebsocketService} from '../../../../core/services/websocket.service';
import {NotificationService} from '../../../../core/services/notification.service';
import {AdService} from '../../../ads/services/ad.service';
import {Ad} from '../../../../shared/models/ad.model';
import {RentalCalendarComponent} from '../../../ads/components/rental-calendar/rental-calendar.component';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {LocationPickerModalComponent, SelectedLocation} from '../../components/location-picker-modal/location-picker-modal.component';
import {ToastService} from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, InitialsPipe, RouterLink, RentalCalendarComponent, LocationPickerModalComponent],
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.css']
})
export class InboxComponent implements OnInit, AfterViewChecked, OnDestroy {

  // Left side
  conversations: ConversationPreview[] = [];
  activeConversation: ConversationPreview | null = null;
  mobileView: 'list' | 'chat' = 'list';

  // Right side
  groupedMessages: MessageGroup[] = [];

  // Calendar (treća kolona)
  currentAdFullDetails: Ad | null = null;
  calendarBlockedIntervals: { start: Date, end: Date }[] = [];

  messages: Message[] =[];
  newMessageContent: string = '';
  myUserId: number | null = null;
  myLocationId: number | null = null;
  isLoadingMessages = false;

  // Attachments
  isUploadingImage = false;
  showLocationPicker = false;

  // Image lightbox
  lightboxImage: string | null = null;

  private messageSub!: Subscription;
  private pollSub!: Subscription;
  private userSub!: Subscription;
  private queryParamSub!: Subscription;
  private conversationsLoaded = false;
  private pendingChatCheck = false;
  private adFetchCancel$ = new Subject<void>();

  // Auto-scroll to bottom
  @ViewChild('chatScroll') private chatScrollContainer!: ElementRef;
  private scrollToBottomNeeded = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private websocketService: WebsocketService,
    private notificationService: NotificationService,
    private adService: AdService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toast: ToastService
  ) {}

  ngOnInit(): void {

    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.myUserId = user.id;
      this.myLocationId = user.locationId ?? null;
    }

    this.userSub = this.authService.currentUser$.subscribe(u => {
      if (u && u.id) {
        this.myUserId = u.id;
        this.myLocationId = u.locationId ?? null;
      }
    });

    this.loadConversations();
    this.queryParamSub = this.route.queryParamMap.subscribe(() => {
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
        this.updateGroupedMessages();
        this.scrollToBottomNeeded = true;
      }
      // Scenario C: message arrived for a different (background) conversation
      else if (msg.senderId !== this.myUserId) {
        this.notificationService.onNewMessageInBackground();
      }

      // Refresh left list so chat jumps to top
      this.loadConversations();

      // Force UI update for new bubbles
      this.cdr.detectChanges();
    }

  loadConversations() {
    this.chatService.getMyConversations().subscribe({
      next: res => {
        this.conversations = res.content;
        this.notificationService.updateFromConversations(this.conversations);
        this.conversationsLoaded = true;
        if (this.pendingChatCheck) {
          this.pendingChatCheck = false;
          this.checkForNewChatRequest();
        }
      },
      error: () => {
        this.conversationsLoaded = true;
        this.pendingChatCheck = false;
      }
    });
  }

  openConversation(conv: ConversationPreview) {
    const prevUnread = conv.unreadCount || 0;
    this.activeConversation = conv;
    if (typeof window !== 'undefined' && window.innerWidth <= 900) {
      this.mobileView = 'chat';
    }
    conv.unreadCount = 0;
    this.notificationService.onConversationOpened(prevUnread);

    // Reset kalendara pri svakom prelasku na novu konverzaciju
    this.currentAdFullDetails = null;
    this.calendarBlockedIntervals = [];

    if (conv.id === 0) {
      this.messages = [];
      this.groupedMessages = [];
      this.isLoadingMessages = false;
      this.scrollToBottomNeeded = true;
    } else {
      this.isLoadingMessages = true;
      this.chatService.getMessages(conv.id).subscribe({
        next: (res) => {
          this.messages = res.content;
          this.updateGroupedMessages();
          this.isLoadingMessages = false;
          this.scrollToBottomNeeded = true;
        },
        error: () => {
          this.isLoadingMessages = false;
        }
      });
    }

    // Učitaj detalje oglasa za prikaz kalendara (otkaži prethodni zahtev)
    this.adFetchCancel$.next();
    this.adService.getAdById(conv.adId).pipe(takeUntil(this.adFetchCancel$)).subscribe({
      next: (ad) => {
        this.currentAdFullDetails = ad;
        this.calendarBlockedIntervals = (ad.blockedIntervals || []).map(interval => ({
          start: new Date(interval.from),
          end: new Date(interval.to),
        }));
      },
      error: () => {
        // Ako oglas nije dostupan (obrisan itd.), kalendar se neće prikazati
        this.currentAdFullDetails = null;
      }
    });
  }

  sendMessage() {
    if (!this.newMessageContent.trim() || !this.activeConversation) return;

    const content = this.newMessageContent.trim();
    const request = {
      adId: this.activeConversation.adId,
      receiverId: this.activeConversation.otherParticipant.id,
      messageType: 'REGULAR' as const,
      content
    };

    this.newMessageContent = '';

    this.appendOptimisticMessage({ content, messageType: 'REGULAR' });
    // Send via WebSocket
    this.websocketService.sendMessage('/app/chat.send', request);
  }

  triggerImagePicker(fileInput: HTMLInputElement): void {
    if (this.isUploadingImage) return;
    fileInput.value = '';
    fileInput.click();
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0 || !this.activeConversation) return;

    const file = input.files[0];
    input.value = '';

    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/heic', 'image/heif'];
    if (!allowed.includes(file.type.toLowerCase()) && !/\.(heic|heif)$/i.test(file.name)) {
      this.toast.showError('Nedozvoljen format slike.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.toast.showError('Slika je veća od 10MB.');
      return;
    }

    this.isUploadingImage = true;
    this.chatService.uploadChatImage(file).subscribe({
      next: (url) => {
        this.isUploadingImage = false;
        if (!url || !this.activeConversation) return;
        this.appendOptimisticMessage({ imageUrl: url, messageType: 'IMAGE' });
        this.websocketService.sendMessage('/app/chat.send', {
          adId: this.activeConversation.adId,
          receiverId: this.activeConversation.otherParticipant.id,
          messageType: 'IMAGE',
          imageUrl: url,
        });
      },
      error: () => {
        this.isUploadingImage = false;
        this.toast.showError('Greška pri otpremanju slike.');
      }
    });
  }

  openLocationPicker(): void {
    if (!this.activeConversation) return;
    this.showLocationPicker = true;
  }

  closeLocationPicker(): void {
    this.showLocationPicker = false;
  }

  onLocationPicked(loc: SelectedLocation): void {
    if (!this.activeConversation) return;
    this.showLocationPicker = false;

    this.appendOptimisticMessage({
      messageType: 'LOCATION',
      locationLat: loc.lat,
      locationLng: loc.lng,
      locationLabel: loc.label,
    });
    this.websocketService.sendMessage('/app/chat.send', {
      adId: this.activeConversation.adId,
      receiverId: this.activeConversation.otherParticipant.id,
      messageType: 'LOCATION',
      locationLat: loc.lat,
      locationLng: loc.lng,
      locationLabel: loc.label,
    });
  }

  openImage(url: string | undefined): void {
    if (!url) return;
    this.lightboxImage = url;
  }

  closeLightbox(): void {
    this.lightboxImage = null;
  }

  mapLinkFor(msg: Message): string {
    return `https://www.google.com/maps?q=${msg.locationLat},${msg.locationLng}`;
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
    } catch { }
  }

  ngOnDestroy(): void {
    if (this.messageSub) this.messageSub.unsubscribe();
    if (this.pollSub) this.pollSub.unsubscribe();
    if (this.userSub) this.userSub.unsubscribe();
    if (this.queryParamSub) this.queryParamSub.unsubscribe();
    this.adFetchCancel$.next();
    this.adFetchCancel$.complete();
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

  private appendOptimisticMessage(partial: Partial<Message> & { messageType: Message['messageType'] }): void {
    if (!this.activeConversation) return;
    const tempMsg: Message = {
      id: -Date.now(),
      conversationId: this.activeConversation.id,
      senderId: this.myUserId || 0,
      content: partial.content ?? '',
      read: true,
      messageType: partial.messageType,
      imageUrl: partial.imageUrl,
      locationLat: partial.locationLat,
      locationLng: partial.locationLng,
      locationLabel: partial.locationLabel,
      createdAt: new Date().toISOString()
    };
    (tempMsg as any)._temp = true;
    this.messages.push(tempMsg);
    this.updateGroupedMessages();
    this.scrollToBottomNeeded = true;
  }

  private removeOptimisticDuplicate(msg: Message): void {
    const idx = this.messages.findIndex(m => {
      if (!(m as any)._temp) return false;
      if (m.senderId !== msg.senderId) return false;
      if (m.messageType !== msg.messageType) return false;
      const sameConv = m.conversationId === msg.conversationId || m.conversationId === 0;
      if (!sameConv) return false;
      if (msg.messageType === 'IMAGE') return m.imageUrl === msg.imageUrl;
      if (msg.messageType === 'LOCATION') {
        return m.locationLat === msg.locationLat && m.locationLng === msg.locationLng;
      }
      return m.content === msg.content;
    });
    if (idx >= 0) {
      this.messages.splice(idx, 1);
    }
  }

  private startPolling(): void {
    this.pollSub = interval(5000).pipe(
      exhaustMap(() => {
        if (!this.websocketService.isConnected()) {
          this.websocketService.connect();
        }
        const convs$ = this.chatService.getMyConversations().pipe(catchError(() => of(null)));
        const needsMsgs = this.activeConversation &&
          this.activeConversation.id !== 0 &&
          !this.messages.some(m => (m as any)._temp);
        const msgs$ = needsMsgs
          ? this.chatService.getMessages(this.activeConversation!.id).pipe(catchError(() => of(null)))
          : of(null);
        return forkJoin([convs$, msgs$]);
      })
    ).subscribe(([convRes, msgRes]) => {
      if (convRes) {
        this.conversations = convRes.content;
        this.notificationService.updateFromConversations(this.conversations);
        this.conversationsLoaded = true;
      }
      if (msgRes) {
        this.messages = msgRes.content;
        this.updateGroupedMessages();
        if (this.isScrolledToBottom()) {
          this.scrollToBottomNeeded = true;
        }
      }
    });
  }

  private isScrolledToBottom(): boolean {
    try {
      const el = this.chatScrollContainer?.nativeElement;
      if (!el) return true;
      return el.scrollHeight - el.scrollTop - el.clientHeight < 60;
    } catch { return true; }
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

  backToList(): void {
    this.mobileView = 'list';
  }

  goToContract(contractId?: number): void {
    if (!contractId) return;
    this.router.navigate(['/user/me/contracts'], { queryParams: { contractId } });
  }

  private isSameDay(d1: Date, d2: Date): boolean {
    return d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate();
  }
}
