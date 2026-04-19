import { Routes } from '@angular/router';
import { AdminShellComponent } from './pages/admin-shell/admin-shell.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './pages/admin-users/admin-users.component';
import { AdminAdsComponent } from './pages/admin-ads/admin-ads.component';
import { AdminContractsComponent } from './pages/admin-contracts/admin-contracts.component';
import { AdminTransactionsComponent } from './pages/admin-transactions/admin-transactions.component';
import { AdminCreditsComponent } from './pages/admin-credits/admin-credits.component';
import { AdminReportsComponent } from './pages/admin-reports/admin-reports.component';
import { AdminVerificationsComponent } from './pages/admin-verifications/admin-verifications.component';
import { adminGuard } from './guards/admin.guard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminShellComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'ads', component: AdminAdsComponent },
      { path: 'contracts', component: AdminContractsComponent },
      { path: 'transactions', component: AdminTransactionsComponent },
      { path: 'credits', component: AdminCreditsComponent },
      { path: 'reports', component: AdminReportsComponent },
      { path: 'verifications', component: AdminVerificationsComponent },
    ]
  }
];
