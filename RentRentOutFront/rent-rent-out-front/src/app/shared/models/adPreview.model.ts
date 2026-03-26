export type PromotionType = 'FEATURED' | 'PRIORITY' | 'HIGHLIGHTED';

export interface AdPreview {
  id: number;
  title: string;
  city: string;
  municipality: string;
  description:  string;
  price: number;
  priceInterval: string;
  currency: string;
  thumbnail:  string;
  adStatus: string;
  viewCount?: number;
  saveCount?: number;
  saved?: boolean;
  createdAt?: string;
  pricePerWeek?: number;
  pricePerMonth?: number;
  expiresAt?: string;
  promotionType?: PromotionType | null;
}

export interface Page<T>{
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
