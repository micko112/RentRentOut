import { Routes } from '@angular/router';
import {AdListComponent} from './features/ads/pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './features/ads/pages/ad-details/ad-details.component';

export const routes: Routes = [
  {path:'', component: AdListComponent },
  {path:'ads/:id', component: AdDetailsComponent}
];
