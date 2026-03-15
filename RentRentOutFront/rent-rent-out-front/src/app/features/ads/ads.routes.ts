import {Routes} from '@angular/router';
import {AdListComponent} from './pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './pages/ad-details/ad-details.component';
import {CreateAdComponent} from './pages/create-ad/create-ad.component';
import {authGuard} from '../auth/auth.guard';
import {EditAdComponent} from './pages/edit-ad/edit-ad.component';

export const ADS_ROUTES: Routes = [

  {
    path: 'create',
    component: CreateAdComponent,
    canActivate:[authGuard],
  },
  {
    path: 'edit/:id',
    component: EditAdComponent,
    canActivate: [authGuard],
  },
  {
    path: ':id',
    component: AdDetailsComponent
  },

  {
    path: '',
    component: AdListComponent
  },
]
