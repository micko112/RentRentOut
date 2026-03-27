import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService, AdReport } from '../../services/admin.service';
import { Page } from '../../../../shared/models/adPreview.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css',
})
export class AdminReportsComponent implements OnInit {
  reports: AdReport[] = [];
  loading = true;
  page = 0;
  totalPages = 0;
  onlyUnreviewed = true;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.adminService.getReports(this.page, 25, this.onlyUnreviewed).subscribe({
      next: (data: Page<AdReport>) => {
        this.reports = data.content;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  toggleFilter() {
    this.onlyUnreviewed = !this.onlyUnreviewed;
    this.page = 0;
    this.load();
  }

  markReviewed(report: AdReport) {
    this.adminService.markReportReviewed(report.id).subscribe({
      next: () => {
        report.reviewed = true;
      },
    });
  }

  goToPage(p: number) {
    this.page = p;
    this.load();
  }
}
