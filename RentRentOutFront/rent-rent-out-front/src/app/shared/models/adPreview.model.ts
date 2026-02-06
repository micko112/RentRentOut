export interface AdPreview {
  id: number;
  title: string;
  city: string;
  municipality: string;
  description:  string;
  price: number;
  priceInterval: string;
  thumbnail:  string;

}

export interface Page<T>{
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
