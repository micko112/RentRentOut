import {Routes} from '@angular/router';
import {AdListComponent} from './pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './pages/ad-details/ad-details.component';
import {CreateAdComponent} from './pages/create-ad/create-ad.component';

export const ADS_ROUTES: Routes = [
  {
    path: '',
    component: AdListComponent
  },
  {
    path: 'create',
    component: CreateAdComponent
  },
  {
    path: ':id',
    component: AdDetailsComponent
  },


]
