import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VerificationService, VerificationStatus } from '../../services/verification.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.css',
})
export class VerifyComponent implements OnInit {
  loading = true;
  submitting = false;
  status: VerificationStatus | null = null;

  docFront: File | null = null;
  docBack: File | null = null;
  selfie: File | null = null;

  docFrontPreview: string | null = null;
  docBackPreview: string | null = null;
  selfiePreview: string | null = null;

  constructor(
    private verificationService: VerificationService,
    private toast: ToastService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.loading = true;
    this.verificationService.getMyStatus().subscribe({
      next: s => { this.status = s; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  onFileSelected(event: Event, field: 'docFront' | 'docBack' | 'selfie'): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    if (file.size > 10 * 1024 * 1024) {
      this.toast.showError('Slika je prevelika (maksimum 10MB).');
      return;
    }
    const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
    const isHeic = ext === 'heic' || ext === 'heif';
    if (!['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif'].includes(file.type) && !isHeic) {
      this.toast.showError('Dozvoljeni formati: JPG, PNG, WEBP, HEIC.');
      return;
    }

    this[field] = file;
    const reader = new FileReader();
    reader.onload = () => {
      if (field === 'docFront') this.docFrontPreview = reader.result as string;
      if (field === 'docBack') this.docBackPreview = reader.result as string;
      if (field === 'selfie') this.selfiePreview = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  removeFile(field: 'docFront' | 'docBack' | 'selfie'): void {
    this[field] = null;
    if (field === 'docFront') this.docFrontPreview = null;
    if (field === 'docBack') this.docBackPreview = null;
    if (field === 'selfie') this.selfiePreview = null;
  }

  canSubmit(): boolean {
    return !!this.docFront && !!this.selfie && !this.submitting;
  }

  submit(): void {
    if (!this.canSubmit()) return;
    this.submitting = true;
    this.verificationService.submit(this.docFront!, this.selfie!, this.docBack).subscribe({
      next: (s) => {
        this.status = s;
        this.submitting = false;
        this.toast.showSuccess('Zahtev je uspešno poslat. Čeka pregled administratora.');
        this.docFront = null;
        this.docBack = null;
        this.selfie = null;
        this.docFrontPreview = null;
        this.docBackPreview = null;
        this.selfiePreview = null;
      },
      error: (err) => {
        this.submitting = false;
        const msg = err?.error?.message || 'Greška pri slanju zahteva.';
        this.toast.showError(msg);
      },
    });
  }

  get canResubmit(): boolean {
    return this.status?.status === 'REJECTED' || this.status?.status === 'NONE';
  }
}
