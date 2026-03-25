import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  token = '';
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;
  private redirectTimeout?: ReturnType<typeof setTimeout>;

  constructor(private fb: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (!this.token) {
      this.errorMessage = 'Token nije pronadjen. Molimo koristite link iz emaila.';
    }

    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatch });
  }

  passwordsMatch(group: FormGroup): { mismatch: true } | null {
    const pw = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pw === confirm ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.isSubmitting || this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.resetPassword(this.token, this.form.value.newPassword).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Lozinka je uspesno promenjena. Mozete se prijaviti.';
        this.errorMessage = '';
        this.redirectTimeout = setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err?.error || 'Token je istekao ili vec iskoriscen.';
      }
    });
  }

  ngOnDestroy(): void {
    clearTimeout(this.redirectTimeout);
  }
}
