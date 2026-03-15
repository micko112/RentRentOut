export interface UpdateAdRequest {
  title: string;
  description: string;
  price: number;
  currency: string;
  priceInterval: string;
  totalQuantity: number;
  images: string[];
  categoryId: number;
  locationId: number;
}