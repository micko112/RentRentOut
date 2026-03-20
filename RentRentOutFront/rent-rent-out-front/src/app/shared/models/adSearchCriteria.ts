

export interface AdSearchCriteria{
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  locationId?: number;
  city?: string;
  priceInterval?: string;

  page?: number;
  size?: number;
  sort?: string;
}
