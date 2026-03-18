import {Routes} from '@angular/router';
import {MyProfileComponent} from './pages/my-profile/my-profile.component';
import {MyAdsComponent} from './pages/my-ads/my-ads.component';
import {ProfileDetailsComponent} from './pages/profile-details/profile-details.component';
import {ContractsComponent} from './pages/contracts/contracts.component';
import {authGuard} from '../auth/auth.guard';
import {ReviewComponent} from '../review/pages/review/review.component';
import {UserProfileComponent} from './public-user/user-profile/user-profile.component';
import {PlaceholderPageComponent} from './pages/placeholder-page/placeholder-page.component';

export const USER_ROUTES: Routes = [
  {
    path: 'me',
    component: MyProfileComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'ads',
        component: MyAdsComponent
      },
      {
        path: 'contracts',
        component: ContractsComponent
      },
      { path: 'notifications', component: PlaceholderPageComponent, data: { title: 'Obaveštenja' } },
      { path: 'reviews', component: PlaceholderPageComponent, data: { title: 'Moje ocene' } },
      { path: 'following', component: PlaceholderPageComponent, data: { title: 'Pratim' } },
      { path: 'saved-searches', component: PlaceholderPageComponent, data: { title: 'Sačuvane pretrage' } },
      { path: 'address-book', component: PlaceholderPageComponent, data: { title: 'Adresar' } },
      { path: 'credit', component: PlaceholderPageComponent, data: { title: 'Kredit' } },
      { path: 'settings', component: PlaceholderPageComponent, data: { title: 'Moj nalog / Podešavanja' } },
      {
        path: '',
        component: ProfileDetailsComponent
      },
    ]
  },
  {path:':id/reviews',
    component: ReviewComponent},
  {path: ':id',
  component: UserProfileComponent},


]
