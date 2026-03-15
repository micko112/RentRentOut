import {Component, OnInit} from '@angular/core';
import {AsyncPipe, CommonModule, DecimalPipe, NgIf} from '@angular/common';
import {Observable} from 'rxjs';
import {User} from '../../../../shared/models/user.model';
import {UserService} from '../../services/user.service';
import {AuthService} from '../../../auth/services/auth.service';
import {RouterModule, RouterOutlet} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ToastService} from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-profile-details',
  standalone: true,
  imports: [
    DecimalPipe,
    NgIf,
    AsyncPipe,
    CommonModule,
    RouterOutlet,
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './profile-details.component.html',
  styleUrl: './profile-details.component.css'
})
export class ProfileDetailsComponent implements OnInit {
  user$!: Observable<User | null>;
  currentUser: User | null = null;

  isEditing = false;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  constructor(private userService: UserService,
              private authService: AuthService,
              private fb: FormBuilder,
              private toastService: ToastService) {
  }

  ngOnInit() {
    this.profileForm = this.fb.group({
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      lastname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      currency: ['', [Validators.required, Validators.minLength(3)]]
    });

    this.passwordForm = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.user$ = this.authService.currentUser$;
    this.user$.subscribe(user => {
      if (!user) return;
      this.currentUser = user;
      this.profileForm.patchValue({
        firstname: user.firstname,
        lastname: user.lastname,
        email: user.email,
        currency: user.currency || 'RSD'
      });
    });
  }

  startEdit(): void {
    this.isEditing = true;
  }

  cancelEdit(): void {
    this.isEditing = false;
    if (this.currentUser) {
      this.profileForm.patchValue({
        firstname: this.currentUser.firstname,
        lastname: this.currentUser.lastname,
        email: this.currentUser.email,
        currency: this.currentUser.currency || 'RSD'
      });
    }
  }

  onSaveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja ispravno.');
      return;
    }

    this.userService.updateMe(this.profileForm.value).subscribe({
      next: (updatedUser) => {
        this.authService.setCurrentUser(updatedUser);
        this.isEditing = false;
        this.toastService.showSuccess('Profil je uspesno azuriran.');
      },
      error: (err) => {
        this.toastService.showError(err?.error?.message || 'Greska pri cuvanju profila.');
      }
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      this.toastService.showError('Unesite ispravne podatke za lozinku.');
      return;
    }

    this.userService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.toastService.showSuccess('Lozinka je promenjena.');
        this.passwordForm.reset();
      },
      error: (err) => {
        this.toastService.showError(err?.error?.message || 'Greska pri promeni lozinke.');
      }
    });
  }
}