import { Component, ElementRef, ViewChild, inject, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../services/support.service';
import { MarkdownPipe } from './markdown.pipe';

@Component({
  selector: 'app-support-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe],
  templateUrl: './support-widget.component.html',
  styleUrl: './support-widget.component.css'
})
export class SupportWidgetComponent implements AfterViewChecked {
  private supportService = inject(SupportService);

  isOpen = false;
  draft = '';
  loading = false;

  @ViewChild('messagesList') messagesList?: ElementRef<HTMLDivElement>;
  @ViewChild('inputRef') inputRef?: ElementRef<HTMLTextAreaElement>;
  private shouldScroll = false;

  get messages() {
    return this.supportService.messages;
  }

  get hasMessages(): boolean {
    return this.supportService.messages.length > 0;
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.shouldScroll = true;
      setTimeout(() => this.inputRef?.nativeElement.focus(), 100);
    }
  }

  close(): void {
    this.isOpen = false;
  }

  onInputKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      this.send();
    }
  }

  send(): void {
    const text = this.draft.trim();
    if (!text || this.loading) return;

    this.supportService.addMessage('user', text);
    this.draft = '';
    this.loading = true;
    this.shouldScroll = true;

    this.supportService.ask(text).subscribe({
      next: reply => {
        this.supportService.addMessage('bot', reply);
        this.loading = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.supportService.addMessage('bot', 'Izvini, trenutno ne mogu da odgovorim. Pokušaj ponovo ili piši na izdajemiznajmljujem.rs@gmail.com');
        this.loading = false;
        this.shouldScroll = true;
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.messagesList) {
      const el = this.messagesList.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScroll = false;
    }
  }

  trackByIndex(i: number): number {
    return i;
  }
}
