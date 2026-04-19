import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  VerificationService,
  AdminVerification,
  AdminVerificationDetails,
  Page,
} from '../../../verification/services/verification.service';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-verifications',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './admin-verifications.component.html',
  styleUrl: './admin-verifications.component.css',
})
export class AdminVerificationsComponent implements OnInit {
  items: AdminVerification[] = [];
  loading = true;
  page = 0;
  totalPages = 0;
  statusFilter: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ALL' = 'PENDING';

  selected: AdminVerificationDetails | null = null;
  detailsLoading = false;
  showRejectForm = false;
  rejectionReason = '';

  constructor(
    private verificationService: VerificationService,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.verificationService.listForAdmin(this.statusFilter, this.page, 20).subscribe({
      next: (data: Page<AdminVerification>) => {
        this.items = data.content;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  changeFilter(status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ALL'): void {
    this.statusFilter = status;
    this.page = 0;
    this.load();
  }

  openDetails(item: AdminVerification): void {
    this.detailsLoading = true;
    this.selected = null;
    this.showRejectForm = false;
    this.rejectionReason = '';
    this.verificationService.getDetailsForAdmin(item.id).subscribe({
      next: (details) => {
        this.selected = details;
        this.detailsLoading = false;
      },
      error: () => {
        this.detailsLoading = false;
        this.toast.showError('Ne mogu da učitam detalje zahteva.');
      },
    });
  }

  closeDetails(): void {
    this.selected = null;
    this.showRejectForm = false;
    this.rejectionReason = '';
  }

  approve(): void {
    if (!this.selected) return;
    if (!confirm('Da li ste sigurni da želite da odobrite ovaj zahtev?')) return;

    const id = this.selected.id;
    this.verificationService.approve(id).subscribe({
      next: () => {
        this.toast.showSuccess('Zahtev je odobren.');
        this.closeDetails();
        this.load();
      },
      error: () => this.toast.showError('Greška pri odobravanju zahteva.'),
    });
  }

  openRejectForm(): void {
    this.showRejectForm = true;
  }

  reject(): void {
    if (!this.selected) return;
    if (!this.rejectionReason.trim()) {
      this.toast.showError('Razlog odbijanja je obavezan.');
      return;
    }

    const id = this.selected.id;
    this.verificationService.reject(id, this.rejectionReason.trim()).subscribe({
      next: () => {
        this.toast.showSuccess('Zahtev je odbijen.');
        this.closeDetails();
        this.load();
      },
      error: () => this.toast.showError('Greška pri odbijanju zahteva.'),
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
