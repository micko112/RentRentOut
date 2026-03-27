import { Routes } from '@angular/router';
import {AdListComponent} from './features/ads/pages/ad-list/ad-list.component';
import {AdDetailsComponent} from './features/ads/pages/ad-details/ad-details.component';
import {LoginComponent} from './features/auth/pages/login/login.component';
import {RegisterComponent} from './features/auth/pages/register/register.component';
import {VerifyEmailComponent} from './features/auth/pages/verify-email/verify-email.component';
import {MyProfileComponent} from './features/user/pages/my-profile/my-profile.component';
import {ReviewFormComponent} from './features/review/components/review-form/review-form.component';
import {ReviewComponent} from './features/review/pages/review/review.component';
import {ForgotPasswordComponent} from './features/auth/pages/forgot-password/forgot-password.component';
import {ResetPasswordComponent} from './features/auth/pages/reset-password/reset-password.component';
import {InboxComponent} from './features/chat/pages/inbox/inbox.component';
import {NotificationsPageComponent} from './features/notifications/pages/notifications-page/notifications-page.component';
import {authGuard} from './features/auth/auth.guard';
import {PrivacyPolicyComponent} from './features/legal/pages/privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './features/legal/pages/terms-of-service/terms-of-service.component';
import {CreditComponent} from './features/user/pages/credit/credit.component';
import {NotFoundComponent} from './shared/pages/not-found/not-found.component';

export const routes: Routes = [
  {path:'ads',
    loadChildren: () => import('./features/ads/ads.routes').then(m=>m.ADS_ROUTES) },
  {path:'user',
    loadChildren: () => import('./features/user/user.routes').then(m=>m.USER_ROUTES)},
  {path:'admin',
    loadChildren: () => import('./features/admin/admin.routes').then(m=>m.ADMIN_ROUTES)},
  {path: 'messages', component: InboxComponent, canActivate: [authGuard]},
  {path: 'notifications', component: NotificationsPageComponent, canActivate: [authGuard]},

  {path: '',
    redirectTo: '/ads',
    pathMatch: 'full'
  },
  {path: 'login',  component: LoginComponent
  },
  {path: 'register',  component: RegisterComponent},
  {path: 'verify-email', component: VerifyEmailComponent},
  {path: 'forgot-password', component: ForgotPasswordComponent},
  {path: 'reset-password', component: ResetPasswordComponent},
  {path: 'privacy-policy', component: PrivacyPolicyComponent},
  {path: 'terms-of-service', component: TermsOfServiceComponent},
  {path: 'credit', component: CreditComponent, canActivate: [authGuard]},

  {
    path: '**',
    component: NotFoundComponent
  },

];
