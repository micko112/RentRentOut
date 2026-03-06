import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Category} from '../../../../shared/models/category.model';
import {AdSearchCriteria} from '../../../../shared/models/adSearchCriteria';

interface LocationOption {
  id: number;
  name: string;
}

@Component({
  selector: 'app-filters-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filters-sidebar.component.html',
  styleUrl: './filters-sidebar.component.css'
})
export class FiltersSidebarComponent {

  @Input() categories: Category[] = [];
  @Input() locations: LocationOption[] = [];

  @Output() applyFilters = new EventEmitter<Partial<AdSearchCriteria>>();

  // jednostavan model – samo ono što backend trenutno podržava
  filters: {
    categoryId?: number;
    locationId?: number;
    minPrice?: number;
    maxPrice?: number;
    keyword?: string;
  } = {};

  currency: 'eur' | 'rsd' = 'eur';        // za UI, ako ti zatreba kasnije

  onSubmit(): void {
    const criteria: Partial<AdSearchCriteria> = {
      keyword: this.filters.keyword,
      categoryId: this.filters.categoryId,
      locationId: this.filters.locationId,
      minPrice: this.filters.minPrice,
      maxPrice: this.filters.maxPrice
      // page / size / sort će rešiti u parentu preko router query parametara
    };

    this.applyFilters.emit(criteria);
  }

  clearAll(): void {
    this.filters = {};
    this.currency = 'eur';
    this.applyFilters.emit({});
  }
}
