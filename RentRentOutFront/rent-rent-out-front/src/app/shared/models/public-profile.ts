import {UserProfile} from './userProfile';
import {Ad} from './ad.model';
import {Review} from './review';
import {AdPreview, Page} from './adPreview.model';

export interface PublicProfile{
  userInfo: UserProfile;
  ads: Page<AdPreview>;
  reviews: Page<Review>;
}
