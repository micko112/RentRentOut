import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';
@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
    registerForm!: FormGroup;
    errorMessage: string | null = null;

    constructor(private fb: FormBuilder,
                private authService: AuthService,
                private router: Router,
                private toastService: ToastService,) {
    }

  ngOnInit() {
    this.registerForm = this.fb.group({
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      lastname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }
  get firstname() { return this.registerForm.get('firstname'); }
  get lastname() { return this.registerForm.get('lastname'); }
  get email() { return this.registerForm.get('email'); }
  get password() { return this.registerForm.get('password'); }

  onSubmit(): void {

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }


    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.router.navigate(['/login']);
        this.toastService.showSuccess("Uspesno ste se registrovali!");
      },
      error: (err) => {
        this.errorMessage = err.error.message || 'Došlo je do greške. Molimo pokušajte ponovo.';
        this.toastService.showError(err);
      }
    });
  }
}
