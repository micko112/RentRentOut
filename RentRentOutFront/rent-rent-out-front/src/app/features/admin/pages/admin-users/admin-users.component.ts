import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { User } from '../../../../shared/models/user.model';
import { Page } from '../../../../shared/models/adPreview.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  usersPage: Page<User> | null = null;
  loading = true;
  currentPage = 0;

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getUsers(this.currentPage).subscribe({
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
}
