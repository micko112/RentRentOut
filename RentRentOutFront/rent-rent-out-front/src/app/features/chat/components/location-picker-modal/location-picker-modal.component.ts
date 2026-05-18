import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LocationService } from '../../../ads/services/location.service';
import { Location } from '../../../../shared/models/location.model';

export interface SelectedLocation {
  lat: number;
  lng: number;
  label: string;
}

@Component({
  selector: 'app-location-picker-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './location-picker-modal.component.html',
  styleUrl: './location-picker-modal.component.css',
})
export class LocationPickerModalComponent implements OnInit {
  /** ID lokacije ulogovanog korisnika - postavlja se kao default predlog (kao na kupujem prodajem) */
  @Input() defaultLocationId: number | null = null;

  @Output() picked = new EventEmitter<SelectedLocation>();
  @Output() closed = new EventEmitter<void>();

  allLocations: Location[] = [];
  filtered: Location[] = [];
  defaultLocation: Location | null = null;
  selectedId: number | null = null;
  search = '';
  loading = true;

  constructor(private locationService: LocationService) {}

  ngOnInit(): void {
    this.locationService.getAll().subscribe({
      next: (list) => {
        this.allLocations = list || [];
        if (this.defaultLocationId) {
          this.defaultLocation = this.allLocations.find(l => l.id === this.defaultLocationId) || null;
          if (this.defaultLocation) {
            this.selectedId = this.defaultLocation.id;
          }
        }
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilter() {
    const q = this.search.trim().toLowerCase();
    if (!q) {
      this.filtered = [...this.allLocations].slice(0, 80);
      return;
    }
    this.filtered = this.allLocations.filter(l => {
      const display = `${l.city} ${l.municipality ?? ''}`.toLowerCase();
      return display.includes(q);
    }).slice(0, 80);
  }

  select(loc: Location) {
    this.selectedId = loc.id;
  }

  formatLabel(loc: Location): string {
    return loc.municipality && loc.municipality !== loc.city
      ? `${loc.city}, ${loc.municipality}`
      : loc.city;
  }

  confirm() {
    const target = this.allLocations.find(l => l.id === this.selectedId);
    if (!target || target.lat == null || target.lng == null) return;
    this.picked.emit({
      lat: Number(target.lat),
      lng: Number(target.lng),
      label: this.formatLabel(target),
    });
  }

  useDefault() {
    if (!this.defaultLocation) return;
    this.selectedId = this.defaultLocation.id;
    this.confirm();
  }

  close() {
    this.closed.emit();
  }
}
