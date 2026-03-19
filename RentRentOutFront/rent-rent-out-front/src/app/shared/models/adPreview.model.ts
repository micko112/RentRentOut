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
  saved?: boolean;
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
