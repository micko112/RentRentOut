export interface UserShort {
  id: number;
  displayName: string;
  avatarUrl: string;
  identified: boolean;
  locationDisplay:  string;
  createdAt: string;
  positiveReviews:  number;
  negativeReviews:   number;
  phoneNumber?: string;
}
