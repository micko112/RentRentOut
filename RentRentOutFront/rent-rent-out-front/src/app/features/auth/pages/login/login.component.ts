import {Component, OnInit, AfterViewInit, NgZone} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';

declare const google: any;
declare const FB: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, AfterViewInit {
  form!: FormGroup;
  submitted = false;
  returnUrl: string = '/';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService,
    private route: ActivatedRoute,
    private ngZone: NgZone,
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
        client_id: '1030670787389-cftefckkjmpgiv41okb87oatffou4e5k.apps.googleusercontent.com',
        callback: (response: any) => this.ngZone.run(() => this.handleGoogleResponse(response))
      });
      google.accounts.id.renderButton(
        document.getElementById('google-btn'),
        { theme: 'outline', size: 'large', width: 300, locale: 'sr' }
      );
    }

  }

  handleGoogleResponse(response: any): void {
    this.authService.googleLogin(response.credential).subscribe({
      next: () => {
        this.toastService.showSuccess('Uspešno ste se prijavili sa Google nalogom!');
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.toastService.showError('Google prijava nije uspela.');
      }
    });
  }

  login() {
    if (this.form.invalid) {
      return;
    }
    const credentials = this.form.value;

    this.authService.login(credentials).subscribe({
      next: () => {
        this.toastService.showSuccess('Uspešno ste se ulogovali!');
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.toastService.showError('Pogrešan email ili lozinka.');
      }
    });
    this.submitted = true;
  }

  loginWithFacebook(): void {
    if (typeof FB === 'undefined') {
      this.toastService.showError('Facebook SDK nije učitan.');
      return;
    }
    FB.login((response: any) => {
      if (response.status === 'connected') {
        const accessToken = response.authResponse.accessToken;
        this.ngZone.run(() => {
          this.authService.facebookLogin(accessToken).subscribe({
            next: () => {
              this.toastService.showSuccess('Uspešno ste se prijavili sa Facebook nalogom!');
              this.router.navigateByUrl(this.returnUrl);
            },
            error: () => {
              this.toastService.showError('Facebook prijava nije uspela.');
            }
          });
        });
      }
    }, { scope: 'public_profile,email' });
  }

}
