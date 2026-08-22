import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SocialAuthButtonsComponent } from '../../components/social-auth-buttons/social-auth-buttons.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink,
    TranslateModule,
    SocialAuthButtonsComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
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
    private translate: TranslateService,
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
    this.returnUrl = this.route.snapshot.queryParams['redirect'] || '/';
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

}
