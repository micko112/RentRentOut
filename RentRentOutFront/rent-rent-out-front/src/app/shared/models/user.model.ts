export interface User{
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  avatarUrl:  string;
  identified:  boolean;
  credit:  number;
  location: string;
  locationId?: number;
  currency: string;
  phoneNumber: string;
  description: string;
  positiveReviews: number;
  negativeReviews: number;
  role: string;
  enabled: boolean;
}
