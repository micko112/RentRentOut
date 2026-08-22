import { AfterViewInit, Component, ElementRef, Input, NgZone, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { environment } from '../../../../../environments/environment';

declare const google: any;
declare const FB: any;

/**
 * Google i Facebook prijava, zajednicka za login i registraciju.
 *
 * Backend za oba socijalna toka koristi isti endpoint i kreira nalog ako
 * ne postoji, pa je "registracija preko Google-a" isti poziv kao i prijava -
 * zato jedna komponenta pokriva obe strane (vidi #7).
 */
@Component({
  selector: 'app-social-auth-buttons',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './social-auth-buttons.component.html',
  styleUrl: './social-auth-buttons.component.css'
})
export class SocialAuthButtonsComponent implements AfterViewInit {

  /** Gde se ide posle uspesne prijave. */
  @Input() redirectTo = '/';

  /** Tekst separatora ispod dugmadi - login i registracija ga formulisu razlicito. */
  @Input() dividerLabelKey = 'auth.login.or_email';

  @ViewChild('googleBtn') googleBtn?: ElementRef<HTMLDivElement>;

  constructor(
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService,
    private ngZone: NgZone,
    private translate: TranslateService,
  ) {}

  ngAfterViewInit(): void {
    if (typeof google === 'undefined' || !this.googleBtn) return;

    google.accounts.id.initialize({
      client_id: environment.googleClientId,
      callback: (response: any) => this.ngZone.run(() => this.handleGoogleResponse(response))
    });
    google.accounts.id.renderButton(
      this.googleBtn.nativeElement,
      { theme: 'outline', size: 'large', width: 300, locale: 'sr' }
    );
  }

  triggerGoogleLogin(): void {
    const gbtn = this.googleBtn?.nativeElement.querySelector('[role="button"]') as HTMLElement | null;
    if (gbtn) {
      gbtn.click();
    } else if (typeof google !== 'undefined') {
      google.accounts.id.prompt();
    } else {
      this.toastService.showError(this.translate.instant('auth.login.toast_google_unavailable'));
    }
  }

  handleGoogleResponse(response: any): void {
    this.authService.googleLogin(response.credential).subscribe({
      next: () => {
        this.toastService.showSuccess(this.translate.instant('auth.login.toast_google_success'));
        this.router.navigateByUrl(this.redirectTo);
      },
      error: () => {
        this.toastService.showError(this.translate.instant('auth.login.toast_google_failed'));
      }
    });
  }

  loginWithFacebook(): void {
    if (typeof FB === 'undefined') {
      this.toastService.showError(this.translate.instant('auth.login.toast_fb_sdk_missing'));
      return;
    }
    FB.login((response: any) => {
      if (response.status === 'connected') {
        const accessToken = response.authResponse.accessToken;
        this.ngZone.run(() => {
          this.authService.facebookLogin(accessToken).subscribe({
            next: () => {
              this.toastService.showSuccess(this.translate.instant('auth.login.toast_fb_success'));
              this.router.navigateByUrl(this.redirectTo);
            },
            error: () => {
              this.toastService.showError(this.translate.instant('auth.login.toast_fb_failed'));
            }
          });
        });
      }
    }, { scope: 'public_profile,email' });
  }
}
