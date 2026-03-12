export interface User{
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  avatarUrl:  string;
  identified:  boolean;
  credit:  number;
  location: string;
  currency: string;
  positiveReviews: number;
  negativeReviews: number;
}
