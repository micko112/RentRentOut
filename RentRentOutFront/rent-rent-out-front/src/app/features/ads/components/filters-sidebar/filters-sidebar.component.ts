import { Component, EventEmitter, Output, Input, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category } from '../../../../shared/models/category.model';
import { AdSearchCriteria } from '../../../../shared/models/adSearchCriteria';
import { Location } from '../../../../shared/models/location.model';
import { CityPickerComponent, CityPickerOption } from '../../../../shared/components/city-picker/city-picker.component';
import { AdService } from '../../services/ad.service';
import { Subject, switchMap, takeUntil, debounceTime, catchError, of } from 'rxjs';

@Component({
  selector: 'app-filters-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule, CityPickerComponent],
  templateUrl: './filters-sidebar.component.html',
  styleUrl: './filters-sidebar.component.css'
})
export class FiltersSidebarComponent implements OnInit, OnDestroy {

  @Input() categories: Category[] = [];

  get parentCategories(): Category[] {
    return this.categories.filter(c => !c.parentId);
  }

  getChildren(parentId: number): Category[] {
    return this.categories.filter(c => c.parentId === parentId);
  }

  private _locations: Location[] = [];
  @Input() set locations(value: Location[]) {
    this._locations = value;
    this.trySyncCityPicker();
  }
  get locations(): Location[] { return this._locations; }

  @Input() set initialCriteria(c: Partial<AdSearchCriteria> | null) {
    if (!c) return;
    this.filters = {
      keyword:       c.keyword,
      categoryId:    c.categoryId,
      minPrice:      c.minPrice,
      maxPrice:      c.maxPrice,
      priceInterval: c.priceInterval,
    };
    this._initialLocationId = c.locationId ?? null;
    this.trySyncCityPicker();
  }

  @Output() applyFilters = new EventEmitter<Partial<AdSearchCriteria>>();

  @ViewChild('cityPicker') cityPicker!: CityPickerComponent;

  cityPickerOption: CityPickerOption | null = null;
  initialLocationId: number | null = null;
  previewCount: number | null = null;

  filters: {
    categoryId?:    number;
    minPrice?:      number;
    maxPrice?:      number;
    keyword?:       string;
    priceInterval?: string;
  } = {};

  readonly intervalOptions = [
    { value: 'PER_HOUR',  label: 'Po satu'   },
    { value: 'PER_DAY',   label: 'Po danu'   },
    { value: 'PER_MONTH', label: 'Po mesecu' },
  ];

  private _initialLocationId: number | null = null;
  private filterChanges$ = new Subject<void>();
  private destroy$       = new Subject<void>();

  constructor(private adService: AdService) {}

  ngOnInit(): void {
    this.filterChanges$.pipe(
      debounceTime(350),
      takeUntil(this.destroy$),
      switchMap(() => this.adService.search({
        ...this.filters,
        city:       this.cityPickerOption?.isCityOnly ? this.cityPickerOption.city : undefined,
        locationId: this.cityPickerOption?.locationId,
        size: 1,
        page: 0,
      }).pipe(catchError(() => of(null))))
    ).subscribe(page => {
      if (page !== null) this.previewCount = page.totalElements;
    });

    // Pokretanje inicijalnog broja odmah po inicijalizaciji
    this.filterChanges$.next();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get activeFilterCount(): number {
    let count = 0;
    if (this.filters.keyword?.trim()) count++;
    if (this.filters.categoryId)       count++;
    if (this.cityPickerOption)          count++;
    if (this.filters.minPrice != null || this.filters.maxPrice != null) count++;
    if (this.filters.priceInterval)    count++;
    return count;
  }

  toggleInterval(value: string): void {
    this.filters.priceInterval = this.filters.priceInterval === value ? undefined : value;
    this.onFilterChange();
  }

  onCityChange(option: CityPickerOption | null): void {
    this.cityPickerOption = option;
    this.onFilterChange();
  }

  onFilterChange(): void {
    this.filterChanges$.next();
  }

  onSubmit(): void {
    this.applyFilters.emit({
      keyword:       this.filters.keyword,
      categoryId:    this.filters.categoryId,
      city:          this.cityPickerOption?.isCityOnly ? this.cityPickerOption.city : undefined,
      locationId:    this.cityPickerOption?.locationId,
      minPrice:      this.filters.minPrice,
      maxPrice:      this.filters.maxPrice,
      priceInterval: this.filters.priceInterval,
    });
  }

  clearAll(): void {
    this.filters = {};
    this.cityPickerOption = null;
    this.cityPicker?.clear();
    this.applyFilters.emit({});
  }

  // Sinhronizuje city picker iz locationId koji je stigao u initialCriteria
  private trySyncCityPicker(): void {
    if (!this._initialLocationId || !this._locations.length) return;
    const loc = this._locations.find(l => l.id === this._initialLocationId);
    if (!loc) return;
    const cityLocs = this._locations.filter(l => l.city === loc.city);
    const muni = loc.municipality?.trim();
    const showMuni = muni && muni !== loc.city && cityLocs.length > 1;
    this.cityPickerOption = {
      label:      showMuni ? `${loc.city} | ${muni}` : loc.city,
      city:       loc.city,
      locationId: loc.id,
      isCityOnly: false
    };
    this.initialLocationId = this._initialLocationId;
  }
}
