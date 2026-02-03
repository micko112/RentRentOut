

export interface AdSearchCriteria{
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  locationId?: number;

  page?: number;
  size?: number;
  sort?: string;
  }
