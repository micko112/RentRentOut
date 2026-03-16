import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../../auth/services/auth.service';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';
import {Message} from '../../../../shared/models/message.model';
import {ConversationPreview} from '../../../../shared/models/conversation-preview.model';

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

  // Za auto-skrol na dno chata
  @ViewChild('chatScroll') private chatScrollContainer!: ElementRef;
  private scrollToBottomNeeded = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {

    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.myUserId = user.id;
    }

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
    if (!this.newMessageContent.trim() || !this.activeConversation) return;

    const request = {
      adId: this.activeConversation.adId,
      receiverId: this.activeConversation.otherParticipant.id,
      content: this.newMessageContent.trim()
    };

    //
    const textToSend = this.newMessageContent;
    this.newMessageContent = '';

    this.chatService.sendMessage(request).subscribe({
      next: (newMsg) => {
        // Dodajemo novu poruku u niz da se odmah pojavi na ekranu
        this.messages.push(newMsg);
        this.scrollToBottomNeeded = true;
        // Osveži levu listu da bi ovaj chat skočio na vrh
        this.loadConversations();
      },
      error: (err) => {
        alert('Greška pri slanju poruke');
        this.newMessageContent = textToSend; // Vraćamo tekst ako pukne
      }
    });
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
}
