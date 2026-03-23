import {Component, OnInit, OnDestroy} from '@angular/core';
import {AsyncPipe, CommonModule, DecimalPipe, NgIf} from '@angular/common';
import {Observable, Subscription} from 'rxjs';
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
export class ProfileDetailsComponent implements OnInit, OnDestroy {
  user$!: Observable<User | null>;
  currentUser: User | null = null;

  isEditing = false;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  private userSub?: Subscription;

  avatarPreview: string | null = null;
  bannerAvatarPreview: string | null = null;
  selectedAvatarFile: File | null = null;
  isUploadingAvatar = false;

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
      currency: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      phoneNumber: ['']
    });

    this.passwordForm = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.user$ = this.authService.currentUser$;
    this.userSub = this.user$.subscribe(user => {
      if (!user) return;
      this.currentUser = user;
      this.profileForm.patchValue({
        firstname: user.firstname,
        lastname: user.lastname,
        email: user.email,
        currency: user.currency || 'RSD',
        description: user.description || '',
        phoneNumber: user.phoneNumber || ''
      });
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

  startEdit(): void {
    this.isEditing = true;
    this.avatarPreview = null;
    this.selectedAvatarFile = null;
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.avatarPreview = null;
    this.selectedAvatarFile = null;
    if (this.currentUser) {
      this.profileForm.patchValue({
        firstname: this.currentUser.firstname,
        lastname: this.currentUser.lastname,
        email: this.currentUser.email,
        currency: this.currentUser.currency || 'RSD',
        description: this.currentUser.description || '',
        phoneNumber: this.currentUser.phoneNumber || ''
      });
    }
  }

  onBannerAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = () => { this.bannerAvatarPreview = reader.result as string; };
    reader.readAsDataURL(file);
    this.isUploadingAvatar = true;
    this.userService.uploadAvatar(file).subscribe({
      next: (urls) => {
        this.isUploadingAvatar = false;
        this.saveProfile(urls[0]);
        this.bannerAvatarPreview = null;
      },
      error: () => {
        this.isUploadingAvatar = false;
        this.bannerAvatarPreview = null;
        this.toastService.showError('Greška pri otpremanju slike.');
      }
    });
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    this.selectedAvatarFile = file;
    const reader = new FileReader();
    reader.onload = () => { this.avatarPreview = reader.result as string; };
    reader.readAsDataURL(file);
  }

  onSaveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja ispravno.');
      return;
    }

    if (this.selectedAvatarFile) {
      this.isUploadingAvatar = true;
      this.userService.uploadAvatar(this.selectedAvatarFile).subscribe({
        next: (urls) => {
          this.isUploadingAvatar = false;
          this.saveProfile(urls[0]);
        },
        error: () => {
          this.isUploadingAvatar = false;
          this.toastService.showError('Greska pri otpremanju slike.');
        }
      });
    } else {
      this.saveProfile(this.currentUser?.avatarUrl);
    }
  }

  private saveProfile(avatarUrl?: string): void {
    const payload = {
      ...this.profileForm.value,
      avatarUrl: avatarUrl || null
    };

    this.userService.updateMe(payload).subscribe({
      next: (updatedUser) => {
        this.authService.setCurrentUser(updatedUser);
        this.isEditing = false;
        this.avatarPreview = null;
        this.selectedAvatarFile = null;
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
