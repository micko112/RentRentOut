import {
  Component, ElementRef, EventEmitter, HostListener, Input,
  OnChanges, Output, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Location } from '../../models/location.model';

export interface CityPickerOption {
  label: string;
  city: string;
  locationId?: number;
  isCityOnly: boolean;
  /** Grad je u brzom izboru (korisnikov grad ili veliki grad) - koristi se za vizuelno isticanje. */
  isPriority?: boolean;
}

/** Gradovi koji uvek idu na vrh liste, redom. Vecina oglasa dolazi odavde. */
const DEFAULT_QUICK_ACCESS_CITIES = ['Beograd', 'Novi Sad'];

@Component({
  selector: 'app-city-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './city-picker.component.html',
  styleUrl: './city-picker.component.css'
})
export class CityPickerComponent implements OnChanges {

  @Input() locations: Location[] = [];
  @Input() allowCityOnly = false;
  @Input() placeholder = 'Svi gradovi';
  @Input() initialLocationId: number | null = null;
  @Input() isInvalid = false;
  /** Korisnikov grad - ide iznad svih ostalih. */
  @Input() priorityCity: string | null = null;

  /** Veliki gradovi odmah ispod korisnikovog. Prosledi [] da se iskljuce. */
  @Input() quickAccessCities: string[] = DEFAULT_QUICK_ACCESS_CITIES;

  @Output() selectionChange = new EventEmitter<CityPickerOption | null>();

  selectedOption: CityPickerOption | null = null;
  showDropdown = false;
  citySearch = '';

  constructor(private el: ElementRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['initialLocationId'] || changes['locations'])
        && this.initialLocationId && this.locations.length > 0) {
      this.setInitialFromLocationId();
    }
  }

  private setInitialFromLocationId(): void {
    const loc = this.locations.find(l => l.id === this.initialLocationId);
    if (!loc) return;
    const cityLocs = this.locations.filter(l => l.city === loc.city);
    const muni = loc.municipality?.trim();
    const showMuni = muni && muni !== loc.city && cityLocs.length > 1;
    this.selectedOption = {
      label: showMuni ? `${loc.city} | ${muni}` : loc.city,
      city: loc.city,
      locationId: loc.id,
      isCityOnly: false
    };
  }

  /**
   * Gradovi koji idu na vrh, redom: prvo korisnikov, pa veliki gradovi.
   * Dedupe cuva prvo pojavljivanje, pa korisnikov grad ostaje najvisi i
   * kad je istovremeno i u quickAccessCities.
   */
  private get priorityOrder(): string[] {
    const wanted = [this.priorityCity, ...this.quickAccessCities]
      .map(c => c?.trim().toLowerCase())
      .filter((c): c is string => !!c);
    return Array.from(new Set(wanted));
  }

  get allOptions(): CityPickerOption[] {
    const result: CityPickerOption[] = [];
    const priorityOrder = this.priorityOrder;
    const rank = (city: string) => priorityOrder.indexOf(city.toLowerCase());

    const uniqueCities = Array.from(new Set(this.locations.map(l => l.city)))
      .sort((a, b) => {
        const aRank = rank(a);
        const bRank = rank(b);
        if (aRank !== -1 && bRank !== -1) return aRank - bRank;
        if (aRank !== -1) return -1;
        if (bRank !== -1) return 1;
        return a.localeCompare(b, 'sr');
      });

    for (const city of uniqueCities) {
      const cityLocs = this.locations
        .filter(l => l.city === city)
        .sort((a, b) => (a.municipality || '').localeCompare(b.municipality || '', 'sr'));

      const isPriority = rank(city) !== -1;

      if (this.allowCityOnly && cityLocs.length > 1) {
        result.push({ label: city, city, isCityOnly: true, isPriority });
      }

      for (const loc of cityLocs) {
        const muni = loc.municipality?.trim();
        const showMuni = muni && muni !== city && cityLocs.length > 1;
        result.push({
          label: showMuni ? `${city} | ${muni}` : city,
          city,
          locationId: loc.id,
          isCityOnly: false,
          isPriority
        });
      }
    }
    return result;
  }

  get filteredOptions(): CityPickerOption[] {
    const search = this.citySearch.trim().toLowerCase();
    if (!search) return this.allOptions;
    const tokens = search.split(/\s+/).filter(t => t.length > 0);
    return this.allOptions.filter(o => {
      const haystack = o.label.toLowerCase().replace(/\|/g, ' ');
      return tokens.every(t => haystack.includes(t));
    });
  }

  get columnSortedOptions(): CityPickerOption[] {
    const items = this.filteredOptions;
    const cols = 4;
    const rows = Math.ceil(items.length / cols);
    if (rows === 0) return items;
    const result: (CityPickerOption | null)[] = new Array(items.length).fill(null);
    for (let i = 0; i < items.length; i++) {
      const col = Math.floor(i / rows);
      const row = i % rows;
      const newIdx = row * cols + col;
      if (newIdx < items.length) result[newIdx] = items[i];
    }
    return result.filter((x): x is CityPickerOption => x !== null);
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) {
      this.citySearch = '';
      setTimeout(() => {
        const el = this.el.nativeElement.querySelector('.city-dropdown') as HTMLElement;
        if (el) el.focus({ preventScroll: true });
      }, 0);
    }
  }

  onDropdownKey(event: KeyboardEvent): void {
    event.stopPropagation();
    if (event.key === 'Escape') { this.showDropdown = false; return; }
    if (event.key === 'Backspace') {
      this.citySearch = this.citySearch.slice(0, -1);
      event.preventDefault();
      return;
    }
    if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
      this.citySearch += event.key.toLowerCase();
      event.preventDefault();
    }
  }

  @HostListener('document:mousedown', ['$event'])
  onOutsideClick(event: MouseEvent): void {
    if (!this.showDropdown) return;
    if (!this.el.nativeElement.contains(event.target as Node)) {
      this.showDropdown = false;
    }
  }

  select(option: CityPickerOption): void {
    this.selectedOption = option;
    this.showDropdown = false;
    this.citySearch = '';
    this.selectionChange.emit(option);
  }

  clear(): void {
    this.selectedOption = null;
    this.citySearch = '';
    this.selectionChange.emit(null);
  }
}
