import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { RentalContract } from '../../../../shared/models/rental-contract.model';
import { Page } from '../../../../shared/models/adPreview.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-admin-contracts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-contracts.component.html',
  styleUrl: './admin-contracts.component.css'
})
export class AdminContractsComponent implements OnInit {
  contractsPage: Page<RentalContract> | null = null;
  loading = true;
  currentPage = 0;

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadContracts();
  }

  loadContracts() {
    this.loading = true;
    this.adminService.getContracts(this.currentPage).subscribe({
      next: (page) => {
        this.contractsPage = page;
        this.loading = false;
      },
      error: () => {
        this.toastService.showError('Greška pri učitavanju ugovora.');
        this.loading = false;
      }
    });
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadContracts();
    }
  }

  nextPage() {
    if (this.contractsPage && !this.contractsPage.last) {
      this.currentPage++;
      this.loadContracts();
    }
  }
}
