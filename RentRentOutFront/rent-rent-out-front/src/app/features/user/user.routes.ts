import {Routes} from '@angular/router';
import {MyProfileComponent} from './pages/my-profile/my-profile.component';
import {MyAdsComponent} from './pages/my-ads/my-ads.component';
import {ProfileDetailsComponent} from './pages/profile-details/profile-details.component';
import {ContractsComponent} from './pages/contracts/contracts.component';

export const USER_ROUTES: Routes = [
  {
    path: 'me',
    component: MyProfileComponent,
    children: [
      {
        path: '',
        component: ProfileDetailsComponent
      },
      {
        path: 'ads',
        component: MyAdsComponent
      },
      {
        path: 'contracts',
        component: ContractsComponent
      }

    ]
  },
]
