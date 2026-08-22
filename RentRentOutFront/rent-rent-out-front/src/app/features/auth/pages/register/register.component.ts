import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SocialAuthButtonsComponent } from '../../components/social-auth-buttons/social-auth-buttons.component';
@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    TranslateModule,
    SocialAuthButtonsComponent
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
    registerForm!: FormGroup;
    errorMessage: string | null = null;
    isSubmitting = false;

    constructor(private fb: FormBuilder,
                private authService: AuthService,
                private router: Router,
                private toastService: ToastService,
                private translate: TranslateService,) {
    }

  ngOnInit() {
    this.registerForm = this.fb.group({
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      lastname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      termsAccepted: [false, Validators.requiredTrue]
    });
  }
  get firstname() { return this.registerForm.get('firstname'); }
  get lastname() { return this.registerForm.get('lastname'); }
  get email() { return this.registerForm.get('email'); }
  get password() { return this.registerForm.get('password'); }
  get termsAccepted() { return this.registerForm.get('termsAccepted'); }

  onSubmit(): void {
    if (this.isSubmitting) return;
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const { termsAccepted: _, ...payload } = this.registerForm.value;
    this.authService.register(payload).subscribe({
      next: () => {
        this.router.navigate(['/login']);
        this.toastService.showSuccess(this.translate.instant('auth.register.toast_success'));
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || err.error || this.translate.instant('auth.register.toast_error');
        this.toastService.showError(this.errorMessage!);
      }
    });
  }
}
