import { Component, ElementRef, HostBinding, Input, ViewChild, inject, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SupportService } from '../services/support.service';
import { MarkdownPipe } from './markdown.pipe';

@Component({
  selector: 'app-support-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe, TranslateModule],
  templateUrl: './support-widget.component.html',
  styleUrl: './support-widget.component.css'
})
export class SupportWidgetComponent implements AfterViewChecked {
  private supportService = inject(SupportService);
  private translate = inject(TranslateService);

  /**
   * Stranica ispod ima sticky traku sa akcijama pri dnu (npr. Objavi u
   * wizard-u), pa se plutajuce dugme mora podici da je ne prekriva.
   */
  @Input() liftedAboveActionBar = false;

  @HostBinding('class.lifted-above-action-bar')
  get isLifted(): boolean {
    return this.liftedAboveActionBar;
  }

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
    this.supportService.addMessage('bot', '');
    this.draft = '';
    this.loading = true;
    this.shouldScroll = true;

    let acc = '';
    this.supportService.askStream(text, {
      onToken: token => {
        acc += token;
        this.supportService.updateLastBotMessage(acc);
        this.shouldScroll = true;
      },
      onDone: () => {
        if (!acc) {
          this.supportService.updateLastBotMessage(this.translate.instant('support.error_fallback'));
        }
        this.loading = false;
        this.shouldScroll = true;
      },
      onError: () => {
        this.supportService.updateLastBotMessage(this.translate.instant('support.error_fallback'));
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
