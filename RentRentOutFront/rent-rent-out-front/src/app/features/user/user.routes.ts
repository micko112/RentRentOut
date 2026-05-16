import {Routes} from '@angular/router';
import {MyProfileComponent} from './pages/my-profile/my-profile.component';
import {MyAdsComponent} from './pages/my-ads/my-ads.component';
import {ProfileDetailsComponent} from './pages/profile-details/profile-details.component';
import {ContractsComponent} from './pages/contracts/contracts.component';
import {authGuard} from '../auth/auth.guard';
import {ReviewComponent} from '../review/pages/review/review.component';
import {UserProfileComponent} from './public-user/user-profile/user-profile.component';
import {PlaceholderPageComponent} from './pages/placeholder-page/placeholder-page.component';
import {SavedAdsComponent} from './pages/saved-ads/saved-ads.component';

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
      { path: 'notifications', redirectTo: '/notifications', pathMatch: 'full' },
      { path: 'reviews', component: PlaceholderPageComponent, data: { title: 'Moje ocene' } },
      { path: 'following', component: PlaceholderPageComponent, data: { title: 'Pratim' } },
      { path: 'saved-searches', component: PlaceholderPageComponent, data: { title: 'Sačuvane pretrage' } },
      { path: 'address-book', component: PlaceholderPageComponent, data: { title: 'Adresar' } },
      { path: 'credit', redirectTo: '/credit', pathMatch: 'full' },
      { path: 'settings', redirectTo: '/user/me', pathMatch: 'full' },
      {
        path: '',
        component: ProfileDetailsComponent
      },
    ]
  },
  {
    path: 'saved-ads',
    component: SavedAdsComponent,
    canActivate: [authGuard]
  },
  {path:':id/reviews',
    component: ReviewComponent},
  {path: ':id',
  component: UserProfileComponent},


]
