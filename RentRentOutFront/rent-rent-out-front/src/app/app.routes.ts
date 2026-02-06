import { Routes } from '@angular/router';
import {AdListComponent} from './features/ads/pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './features/ads/pages/ad-details/ad-details.component';

export const routes: Routes = [
  {path:'ads', loadChildren: () => import('./features/ads/ads.routes').then(m=>m.ADS_ROUTES) },
  {path: '',
    redirectTo: '/ads',
    pathMatch: 'full'},

  {
    path: '**',
    redirectTo: '/ads'
  }
];
