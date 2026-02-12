import {Routes} from '@angular/router';
import {MyProfileComponent} from './pages/my-profile/my-profile.component';
import {MyAdsComponent} from './pages/my-ads/my-ads.component';
import {ProfileDetailsComponent} from './pages/profile-details/profile-details.component';

export const USER_ROUTES: Routes = [
  {
    path: '',
    component: MyProfileComponent,
    children: [
      {
        path: 'me/ads',
        component: MyAdsComponent
      },
      {
        path: 'me',
        component: ProfileDetailsComponent
      }
    ]
  },

]
