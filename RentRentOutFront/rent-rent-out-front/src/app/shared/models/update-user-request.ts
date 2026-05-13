export interface UpdateUserRequest {
  firstname: string;
  lastname: string;
  email: string;
  currency: string;
  description?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  locationId?: number | null;
}
