import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';

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
                private router: Router,) {
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
    // Proveri da li je forma validna
    if (this.registerForm.invalid) {
      // Markiraj sva polja kao "dodirnuta" da se prikažu greške
      this.registerForm.markAllAsTouched();
      return;
    }

    // Pozovi servis
    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        // Uspeh! Preusmeri na login stranicu sa porukom.
        // Kasnije ovde možeš dodati i "flash" poruku "Uspešno ste se registrovali!"
        this.router.navigate(['/login']);
      },
      error: (err) => {
        // Prikaz greške sa backenda (npr. "Email already exists")
        this.errorMessage = err.error.message || 'Došlo je do greške. Molimo pokušajte ponovo.';
        console.error('Registration failed', err);
      }
    });
  }
}
