import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { AdminService } from '../../services/admin.service';
import { User } from '../../../../shared/models/user.model';
import { Page } from '../../../../shared/models/adPreview.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { PromotionService } from '../../../ads/services/promotion.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  usersPage: Page<User> | null = null;
  loading = true;
  currentPage = 0;
  searchQuery = '';

  // Credit modal
  showCreditModal = false;
  creditUser: User | null = null;
  creditAmount: number | null = null;
  creditDescription = '';
  addingCredit = false;

  constructor(
    private adminService: AdminService,
    private toastService: ToastService,
    private promotionService: PromotionService
  ) {}

  ngOnInit() {
    this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadUsers();
    });
    this.loadUsers();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchChange() {
    this.searchSubject.next(this.searchQuery);
  }

  trackByUser(_: number, u: User) { return u.id; }

  loadUsers() {
    this.loading = true;
    this.adminService.getUsers(this.currentPage, 20, this.searchQuery).subscribe({
      next: (page) => {
        this.usersPage = page;
        this.loading = false;
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju korisnika.');
        this.loading = false;
      }
    });
  }

  toggleUser(user: User) {
    this.adminService.toggleUserEnabled(user.id).subscribe({
      next: () => {
        const action = user.enabled ? 'deaktiviran' : 'aktiviran';
        this.toastService.showSuccess(`Korisnik je ${action}.`);
        this.loadUsers();
      },
      error: () => {
        this.toastService.showError('Greška pri promeni statusa korisnika.');
      }
    });
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  nextPage() {
    if (this.usersPage && !this.usersPage.last) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  openCreditModal(user: User) {
    this.creditUser = user;
    this.creditAmount = null;
    this.creditDescription = '';
    this.showCreditModal = true;
  }

  closeCreditModal() {
    this.showCreditModal = false;
    this.creditUser = null;
  }

  confirmAddCredit() {
    if (!this.creditUser || !this.creditAmount || this.creditAmount <= 0 || this.addingCredit) return;
    this.addingCredit = true;
    this.promotionService.addCredit(this.creditUser.id, this.creditAmount, this.creditDescription).subscribe({
      next: () => {
        this.toastService.showSuccess(`Dodato ${this.creditAmount} RSD korisniku ${this.creditUser!.firstname}.`);
        this.addingCredit = false;
        this.closeCreditModal();
        this.loadUsers();
      },
      error: () => {
        this.toastService.showError('Greška pri dodavanju kredita.');
        this.addingCredit = false;
      }
    });
  }
}
