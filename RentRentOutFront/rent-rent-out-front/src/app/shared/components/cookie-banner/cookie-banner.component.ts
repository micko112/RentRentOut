import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CookieConsentService } from '../../services/cookie-consent.service';

@Component({
  selector: 'app-cookie-banner',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cookie-banner.component.html',
  styleUrl: './cookie-banner.component.css'
})
export class CookieBannerComponent {
  private consentService = inject(CookieConsentService);

  status$ = this.consentService.status$;

  accept(): void {
    this.consentService.accept();
  }

  decline(): void {
    this.consentService.decline();
  }
}
