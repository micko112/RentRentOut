import { CommonModule } from '@angular/common';
import { Component, HostListener, Input, OnInit, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AppLang, LanguageService } from '../../../core/services/language.service';

interface LangOption {
  code: AppLang;
  label: string;
  flag: string;
}

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './language-switcher.component.html',
  styleUrls: ['./language-switcher.component.css'],
})
export class LanguageSwitcherComponent implements OnInit {
  private langService = inject(LanguageService);

  options: LangOption[] = [
    { code: 'sr', label: 'SR', flag: 'SR' },
    { code: 'en', label: 'EN', flag: 'EN' },
  ];

  /** 'below' (default) opens dropdown under button; 'right' opens it to the right of the icon (used inside sidebar drawer). */
  @Input() menuPosition: 'below' | 'right' = 'below';

  current: AppLang = 'sr';
  open = false;

  ngOnInit(): void {
    this.langService.lang$.subscribe((l) => (this.current = l));
  }

  toggle(): void {
    this.open = !this.open;
  }

  choose(lang: AppLang): void {
    this.open = false;
    if (lang !== this.current) {
      this.langService.setLang(lang).then(() => {
        // Full reload so all server-rendered content (categories, DTOs
        // that depend on Accept-Language) refetches with the new locale.
        if (typeof window !== 'undefined') {
          window.location.reload();
        }
      });
    }
  }

  get currentOption(): LangOption {
    return this.options.find((o) => o.code === this.current) ?? this.options[0];
  }

  @HostListener('document:click', ['$event'])
  onDocClick(event: MouseEvent): void {
    if (!this.open) return;
    const target = event.target as HTMLElement;
    if (!target.closest('.lang-switcher')) {
      this.open = false;
    }
  }
}
