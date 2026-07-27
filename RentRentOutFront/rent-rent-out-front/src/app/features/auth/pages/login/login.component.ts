import {Component, OnInit, AfterViewInit, NgZone} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';
import { environment } from '../../../../../environments/environment';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

declare const google: any;
declare const FB: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink,
    TranslateModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, AfterViewInit {
  form!: FormGroup;
  submitted = false;
  isLoggingIn = false;
  returnUrl: string = '/';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService,
    private route: ActivatedRoute,
    private ngZone: NgZone,
    private translate: TranslateService,
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
    this.returnUrl = this.route.snapshot.queryParams['redirect'] || '/';
  }

  ngAfterViewInit(): void {
    if (typeof google !== 'undefined') {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => this.ngZone.run(() => this.handleGoogleResponse(response))
      });
      google.accounts.id.renderButton(
        document.getElementById('google-btn'),
        { theme: 'outline', size: 'large', width: 300, locale: 'sr' }
      );
    }

  }

  triggerGoogleLogin(): void {
    const gbtn = document.querySelector('#google-btn [role="button"]') as HTMLElement | null;
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
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.toastService.showError(this.translate.instant('auth.login.toast_google_failed'));
      }
    });
  }

  login() {
    this.submitted = true;
    if (this.isLoggingIn || this.form.invalid) {
      return;
    }
    this.isLoggingIn = true;
    const credentials = this.form.value;

    this.authService.login(credentials).subscribe({
      next: () => {
        this.isLoggingIn = false;
        this.toastService.showSuccess(this.translate.instant('auth.login.toast_login_success'));
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.isLoggingIn = false;
        this.toastService.showError(this.translate.instant('auth.login.toast_wrong_credentials'));
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
              this.router.navigateByUrl(this.returnUrl);
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
