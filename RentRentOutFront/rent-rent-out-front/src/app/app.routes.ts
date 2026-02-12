import { Routes } from '@angular/router';
import {AdListComponent} from './features/ads/pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './features/ads/pages/ad-details/ad-details.component';
import {LoginComponent} from './features/auth/pages/login/login.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';
import {MyProfileComponent} from './features/user/pages/my-profile/my-profile.component';

export const routes: Routes = [
  {path:'ads', loadChildren: () => import('./features/ads/ads.routes').then(m=>m.ADS_ROUTES) },
  {path:'me', loadChildren: () => import('./features/user/user.routes').then(m=>m.USER_ROUTES)},
  {path: '',
    redirectTo: '/ads',
    pathMatch: 'full'
  },
  {path: 'login',  component: LoginComponent
  },


  {path: 'register',  component: RegisterComponent},
  {
    path: '**',
    redirectTo: '/ads'
  },



];
