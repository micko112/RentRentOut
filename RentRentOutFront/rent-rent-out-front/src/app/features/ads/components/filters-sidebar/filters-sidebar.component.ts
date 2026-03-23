import { Component, EventEmitter, Output, Input, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category } from '../../../../shared/models/category.model';
import { AdSearchCriteria } from '../../../../shared/models/adSearchCriteria';
import { Location } from '../../../../shared/models/location.model';
import { CityPickerComponent, CityPickerOption } from '../../../../shared/components/city-picker/city-picker.component';

@Component({
  selector: 'app-filters-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule, CityPickerComponent],
  templateUrl: './filters-sidebar.component.html',
  styleUrl: './filters-sidebar.component.css'
})
export class FiltersSidebarComponent {

  @Input() categories: Category[] = [];
  @Input() locations: Location[] = [];
  @Input() totalResults = 0;

  @Output() applyFilters = new EventEmitter<Partial<AdSearchCriteria>>();

  @ViewChild('cityPicker') cityPicker!: CityPickerComponent;

  cityPickerOption: CityPickerOption | null = null;

  filters: {
    categoryId?: number;
    minPrice?: number;
    maxPrice?: number;
    keyword?: string;
    priceInterval?: string;
  } = {};

  readonly intervalOptions = [
    { value: 'PER_HOUR',  label: 'Po satu' },
    { value: 'PER_DAY',   label: 'Po danu' },
    { value: 'PER_MONTH', label: 'Po mesecu' },
  ];

  get activeFilterCount(): number {
    let count = 0;
    if (this.filters.keyword?.trim()) count++;
    if (this.filters.categoryId) count++;
    if (this.cityPickerOption) count++;
    if (this.filters.minPrice != null || this.filters.maxPrice != null) count++;
    if (this.filters.priceInterval) count++;
    return count;
  }

  toggleInterval(value: string): void {
    this.filters.priceInterval = this.filters.priceInterval === value ? undefined : value;
  }

  onCityChange(option: CityPickerOption | null): void {
    this.cityPickerOption = option;
  }

  onSubmit(): void {
    this.applyFilters.emit({
      keyword: this.filters.keyword,
      categoryId: this.filters.categoryId,
      city: this.cityPickerOption?.isCityOnly ? this.cityPickerOption.city : undefined,
      locationId: this.cityPickerOption?.locationId,
      minPrice: this.filters.minPrice,
      maxPrice: this.filters.maxPrice,
      priceInterval: this.filters.priceInterval,
    });
  }

  clearAll(): void {
    this.filters = {};
    this.cityPickerOption = null;
    this.cityPicker?.clear();
    this.applyFilters.emit({});
  }
}
