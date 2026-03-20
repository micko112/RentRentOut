import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {LocationService} from '../../services/location.service';
import {Category} from '../../../../shared/models/category.model';
import {PriceInterval} from '../../../../shared/models/price-interval.enum';
import {ToastService} from '../../../../shared/services/toast.service';
import {Ad} from '../../../../shared/models/ad.model';
import {UpdateAdRequest} from '../../../../shared/models/update-ad-request';
import {Location} from '../../../../shared/models/location.model';

@Component({
  selector: 'app-edit-ad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-ad.component.html',
  styleUrl: './edit-ad.component.css'
})
export class EditAdComponent implements OnInit {

  // ── Step state ──────────────────────────────────────────────────────────
  currentStep = 1;
  readonly totalSteps = 2;

  // ── Form ────────────────────────────────────────────────────────────────
  form!: FormGroup;
  readonly MAX_TITLE = 100;
  readonly MAX_DESC = 2000;

  // ── Categories ──────────────────────────────────────────────────────────
  categories: Category[] = [];

  // ── Price intervals ─────────────────────────────────────────────────────
  readonly priceIntervalOptions = [
    { value: PriceInterval.PER_HOUR,  label: 'Po satu',   unit: '/h'   },
    { value: PriceInterval.PER_DAY,   label: 'Po danu',   unit: '/dan' },
    { value: PriceInterval.PER_MONTH, label: 'Po mesecu', unit: '/mes' },
  ];

  // ── Locations ───────────────────────────────────────────────────────────
  locations: Location[] = [];
  filteredLocations: Location[] = [];
  locationSearch = '';
  showLocationDropdown = false;

  @ViewChild('locTriggerRef') locTriggerRef!: ElementRef;
  locDropdownStyle: {[key: string]: string} = {};

  // ── Images ──────────────────────────────────────────────────────────────
  selectedFiles: File[] = [];
  previewUrl: string[] = [];
  existingImages: string[] = [];
  isDragging = false;
  readonly MAX_IMAGES = 10;

  // ── Misc ────────────────────────────────────────────────────────────────
  adId!: number;
  currentAd!: Ad;
  isLoading = true;

  readonly stepConfig = [
    { label: 'Kategorija i opis' },
    { label: 'Slike, cena i lokacija' },
  ];

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private fb: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private toastService: ToastService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(this.MAX_TITLE)]],
      description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(this.MAX_DESC)]],
      price: [null, [Validators.required, Validators.min(1)]],
      currency: ['RSD', [Validators.required]],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      images: [[], Validators.required],
      pricePerWeek:  [null],
      pricePerMonth: [null],
    });

    const paramId = this.route.snapshot.paramMap.get('id');
    this.adId = paramId ? Number(paramId) : NaN;

    if (!this.adId || Number.isNaN(this.adId)) {
      this.toastService.showError('Neispravan ID oglasa.');
      this.router.navigate(['/user/me/ads']);
      return;
    }

    this.categoryService.getAll().subscribe({
      next: (cats) => { this.categories = cats; },
      error: () => { this.toastService.showError('Greska pri ucitavanju kategorija.'); }
    });

    this.locationService.getAll().subscribe({
      next: (locs) => {
        this.locations = locs;
        this.filteredLocations = locs.slice(0, 30);
      },
      error: () => { this.toastService.showError('Greska pri ucitavanju lokacija.'); }
    });

    this.adService.getAdById(this.adId).subscribe({
      next: (ad) => {
        this.currentAd = ad;
        this.existingImages = [...(ad.images || [])];
        this.ensureLocationInList(ad.location);

        // Pre-fill location search display
        if (ad.location) {
          this.locationSearch = ad.location.municipality
            ? `${ad.location.city}, ${ad.location.municipality}`
            : ad.location.city;
        }

        this.form.patchValue({
          title: ad.title,
          description: ad.description,
          price: ad.price,
          currency: ad.currency,
          priceInterval: ad.priceInterval,
          categoryId: ad.category?.id ?? null,
          locationId: ad.location?.id ?? null,
          totalQuantity: ad.totalQuantity,
          images: this.existingImages,
          pricePerWeek: ad.pricePerWeek ?? null,
          pricePerMonth: ad.pricePerMonth ?? null,
        });

        this.isLoading = false;
      },
      error: () => {
        this.toastService.showError('Ne mogu da ucitam oglas.');
        this.router.navigate(['/user/me/ads']);
      }
    });
  }

  // ════════════════════════════════════════════════════════
  //  NAVIGATION
  // ════════════════════════════════════════════════════════

  get stepProgress(): number {
    return Math.round(((this.currentStep - 1) / (this.totalSteps - 1)) * 100);
  }

  get stepValid(): boolean {
    switch (this.currentStep) {
      case 1:
        return !!this.form.get('categoryId')?.value
          && !this.form.get('title')?.invalid
          && !this.form.get('description')?.invalid
          && this.titleLength >= 5
          && this.descLength >= 20;
      case 2:
        return (this.existingImages.length > 0 || this.selectedFiles.length > 0)
          && !this.form.get('price')?.invalid
          && !!this.form.get('currency')?.value
          && !!this.form.get('locationId')?.value;
      default:
        return false;
    }
  }

  nextStep(): void {
    if (!this.stepValid) { this.markCurrentStepTouched(); return; }
    if (this.currentStep < this.totalSteps) this.currentStep++;
  }

  prevStep(): void {
    if (this.currentStep > 1) this.currentStep--;
  }

  goToStep(step: number): void {
    if (step < this.currentStep) this.currentStep = step;
  }

  private markCurrentStepTouched(): void {
    const map: Record<number, string[]> = {
      1: ['categoryId', 'title', 'description'],
      2: ['images', 'price', 'currency', 'locationId'],
    };
    (map[this.currentStep] ?? []).forEach(f => this.form.get(f)?.markAsTouched());
  }

  // ════════════════════════════════════════════════════════
  //  IMAGES (drag-drop)
  // ════════════════════════════════════════════════════════

  onFileSelected(event: any) {
    const input = event.target as HTMLInputElement;
    if (input.files) this.addFiles(input.files);
    input.value = '';
  }

  onDragOver(e: DragEvent): void  { e.preventDefault(); e.stopPropagation(); this.isDragging = true;  }
  onDragLeave(e: DragEvent): void { e.preventDefault(); e.stopPropagation(); this.isDragging = false; }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging = false;
    if (e.dataTransfer?.files) this.addFiles(e.dataTransfer.files);
  }

  private addFiles(files: FileList): void {
    const totalExisting = this.existingImages.length + this.selectedFiles.length;
    const remaining = this.MAX_IMAGES - totalExisting;
    let added = 0;
    for (let i = 0; i < files.length && added < remaining; i++) {
      const file = files[i];
      if (!file.type.match(/image\/*/)) { this.toastService.showError(`"${file.name}" nije slika.`); continue; }
      if (file.size > 10 * 1024 * 1024) { this.toastService.showError(`"${file.name}" premašuje 10MB.`); continue; }
      this.selectedFiles.push(file);
      this.previewUrl.push(URL.createObjectURL(file));
      added++;
    }
    this.syncImagesControl();
  }

  removeExistingImage(index: number): void {
    this.existingImages.splice(index, 1);
    this.syncImagesControl();
  }

  removeNewImage(index: number): void {
    URL.revokeObjectURL(this.previewUrl[index]);
    this.selectedFiles.splice(index, 1);
    this.previewUrl.splice(index, 1);
    this.syncImagesControl();
  }

  private syncImagesControl(): void {
    const combined = [...this.existingImages, ...this.previewUrl];
    this.form.patchValue({images: combined});
    this.form.get('images')?.updateValueAndValidity();
  }

  get totalImageCount(): number {
    return this.existingImages.length + this.selectedFiles.length;
  }

  // ════════════════════════════════════════════════════════
  //  LOCATION
  // ════════════════════════════════════════════════════════

  onLocationFocus(): void {
    this.showLocationDropdown = true;
    this.filteredLocations = this.locations.slice(0, 30);
    if (this.locTriggerRef) {
      const rect = this.locTriggerRef.nativeElement.getBoundingClientRect();
      this.locDropdownStyle = {
        top: (rect.bottom + 4) + 'px',
        left: rect.left + 'px',
        minWidth: rect.width + 'px',
        width: Math.max(rect.width, 480) + 'px'
      };
    }
  }

  @HostListener('document:mousedown', ['$event'])
  onOutsideLocClick(event: MouseEvent): void {
    if (!this.showLocationDropdown) return;
    const trigger = this.locTriggerRef?.nativeElement;
    if (trigger && !trigger.closest('.loc-wrapper')?.contains(event.target as Node)) {
      this.showLocationDropdown = false;
    }
  }

  onLocationInput(event: Event): void {
    const q = (event.target as HTMLInputElement).value;
    this.locationSearch = q;
    this.showLocationDropdown = true;
    this.form.patchValue({ locationId: null });
    const lower = q.toLowerCase();
    this.filteredLocations = (lower
      ? this.locations.filter(l =>
          l.city.toLowerCase().includes(lower) ||
          (l.municipality ?? '').toLowerCase().includes(lower))
      : this.locations
    ).slice(0, 30);
  }

  selectLocation(loc: Location): void {
    this.form.patchValue({ locationId: loc.id });
    this.locationSearch = loc.municipality ? `${loc.city}, ${loc.municipality}` : loc.city;
    this.showLocationDropdown = false;
  }

  hideLocationDropdown(): void { /* no-op, handled by mousedown */ }

  clearLocation(): void {
    this.form.patchValue({ locationId: null });
    this.locationSearch = '';
  }

  getSelectedLocationDisplay(): string {
    const id = this.form.get('locationId')?.value;
    if (!id) return '';
    const loc = this.locations.find(l => l.id === id);
    return loc ? (loc.municipality ? `${loc.city}, ${loc.municipality}` : loc.city) : '';
  }

  // ════════════════════════════════════════════════════════
  //  PRICING
  // ════════════════════════════════════════════════════════

  get priceDisplay(): string {
    const price = this.form.get('price')?.value;
    if (!price || price <= 0) return '—';
    const sym  = this.selectedCurrency === 'EUR' ? '€' : 'RSD';
    const unit = this.priceIntervalOptions.find(p => p.value === this.selectedInterval)?.unit ?? '';
    return `${Number(price).toLocaleString('sr-RS')} ${sym}${unit}`;
  }

  selectPriceInterval(value: PriceInterval): void {
    this.form.patchValue({ priceInterval: value });
  }

  // ════════════════════════════════════════════════════════
  //  QUANTITY
  // ════════════════════════════════════════════════════════

  increment(): void {
    const v = this.quantity;
    if (v < 999) this.form.patchValue({ totalQuantity: v + 1 });
  }

  decrement(): void {
    const v = this.quantity;
    if (v > 1) this.form.patchValue({ totalQuantity: v - 1 });
  }

  // ════════════════════════════════════════════════════════
  //  GETTERS
  // ════════════════════════════════════════════════════════

  get titleLength(): number      { return this.form.get('title')?.value?.length ?? 0; }
  get descLength(): number       { return this.form.get('description')?.value?.length ?? 0; }
  get quantity(): number         { return this.form.get('totalQuantity')?.value ?? 1; }
  get selectedCurrency(): string { return this.form.get('currency')?.value ?? 'RSD'; }
  get selectedInterval(): string { return this.form.get('priceInterval')?.value ?? PriceInterval.PER_DAY; }

  private ensureLocationInList(location?: Location | null): void {
    if (!location) return;
    const exists = this.locations.some(loc => loc.id === location.id);
    if (!exists) {
      this.locations = [...this.locations, location];
    }
  }

  // ════════════════════════════════════════════════════════
  //  SUBMIT
  // ════════════════════════════════════════════════════════

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja.');
      return;
    }

    const basePayload: Omit<UpdateAdRequest, 'images'> = {
      title: this.form.value.title,
      description: this.form.value.description,
      price: this.form.value.price,
      currency: this.form.value.currency,
      priceInterval: this.form.value.priceInterval,
      totalQuantity: this.form.value.totalQuantity,
      categoryId: this.form.value.categoryId,
      locationId: this.form.value.locationId,
      pricePerWeek: this.form.value.pricePerWeek,
      pricePerMonth: this.form.value.pricePerMonth,
    };

    const finalizeUpdate = (images: string[]) => {
      if (images.length === 0) {
        this.toastService.showError('Morate ostaviti bar jednu sliku.');
        return;
      }

      const payload: UpdateAdRequest = {
        ...basePayload,
        images
      };

      this.adService.updateAd(this.adId, payload).subscribe({
        next: (updatedAd) => {
          this.toastService.showSuccess('Oglas uspesno izmenjen.');
          this.router.navigate(['/ads', updatedAd.id]);
        },
        error: () => {
          this.toastService.showError('Greska pri izmeni oglasa.');
        }
      });
    };

    if (this.selectedFiles.length > 0) {
      this.adService.uploadImages(this.selectedFiles).subscribe({
        next: (uploadedUrls) => {
          finalizeUpdate([...this.existingImages, ...uploadedUrls]);
        },
        error: () => {
          this.toastService.showError('Greska pri upload-u slika.');
        }
      });
    } else {
      finalizeUpdate([...this.existingImages]);
    }
  }
}
