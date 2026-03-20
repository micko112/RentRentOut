import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdService } from '../../services/ad.service';
import { Category } from '../../../../shared/models/category.model';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PriceInterval } from '../../../../shared/models/price-interval.enum';
import { CategoryService } from '../../services/category.service';
import { LocationService } from '../../services/location.service';
import { Router, RouterLink } from '@angular/router';
import { filter, switchMap, take } from 'rxjs';
import { ToastService } from '../../../../shared/services/toast.service';
import { Location } from '../../../../shared/models/location.model';
import { CityPickerComponent, CityPickerOption } from '../../../../shared/components/city-picker/city-picker.component';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-create-ad',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CityPickerComponent],
  standalone: true,
  templateUrl: './create-ad.component.html',
  styleUrl: './create-ad.component.css'
})
export class CreateAdComponent implements OnInit {

  // ── Step state ──────────────────────────────────────────────────────────
  currentStep = 1;
  readonly totalSteps = 2;
  isSubmitting = false;

  // ── Images ──────────────────────────────────────────────────────────────
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  isDragging = false;
  readonly MAX_IMAGES = 10;

  // ── Categories ──────────────────────────────────────────────────────────
  categories: Category[] = [];
  parentCategories: Category[] = [];
  childCategories: Category[] = [];
  selectedParentId: number | null = null;

  // ── Locations ───────────────────────────────────────────────────────────
  locations: Location[] = [];
  initialLocationId: number | null = null;

  // ── Form ────────────────────────────────────────────────────────────────
  form!: FormGroup;
  readonly MAX_TITLE = 100;
  readonly MAX_DESC = 2000;

  readonly priceIntervalOptions = [
    { value: PriceInterval.PER_HOUR,  label: 'Po satu',   unit: '/h'   },
    { value: PriceInterval.PER_DAY,   label: 'Po danu',   unit: '/dan' },
    { value: PriceInterval.PER_MONTH, label: 'Po mesecu', unit: '/mes' },
  ];

  readonly stepConfig = [
    { label: 'Kategorija i opis' },
    { label: 'Slike, cena i lokacija' },
  ];

  private readonly catIconMap: Record<string, string> = {
    alat: '🔧', bušil: '🔧', mašin: '⚙️',
    vozil: '🚗', automobil: '🚗', motor: '🏍️', bicikl: '🚲',
    elektron: '💻', kompjuter: '💻', telefon: '📱',
    sport: '⚽', fitnes: '🏋️', ski: '⛷️',
    muzik: '🎸', instrument: '🎸',
    knjig: '📚',
    kuhin: '🍳',
    namešt: '🛋️',
    kamp: '⛺', planin: '🏔️',
    foto: '📷', kamera: '📷', video: '🎥',
  };

  constructor(
    private adService: AdService,
    private categoryService: CategoryService,
    private locationService: LocationService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(cats => {
      this.categories = cats;
      this.parentCategories = cats.filter(c => !c.parentId);
    });

    this.locationService.getAll().subscribe({
      next: locs => { this.locations = locs; },
      error: () => this.toastService.showError('Greška pri učitavanju lokacija.'),
    });

    this.authService.currentUser$.pipe(
      filter(u => u !== null),
      take(1)
    ).subscribe(user => {
      if (user?.locationId && !this.initialLocationId) {
        this.initialLocationId = user.locationId;
      }
    });

    this.form = this.fb.group({
      title:         ['', [Validators.required, Validators.minLength(5), Validators.maxLength(this.MAX_TITLE)]],
      description:   ['', [Validators.required, Validators.minLength(20), Validators.maxLength(this.MAX_DESC)]],
      price:         [null, [Validators.required, Validators.min(1)]],
      currency:      ['RSD', Validators.required],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId:    [null, Validators.required],
      locationId:    [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1), Validators.max(999)]],
      images:        [[]],
      pricePerWeek:  [null],
      pricePerMonth: [null],
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
      case 1: return !!this.form.get('categoryId')?.value
                     && !this.form.get('title')?.invalid
                     && !this.form.get('description')?.invalid
                     && this.titleLength >= 5
                     && this.descLength >= 20;
      case 2: return this.selectedFiles.length > 0
                     && !this.form.get('price')?.invalid
                     && !!this.form.get('currency')?.value
                     && !!this.form.get('locationId')?.value;
      default: return false;
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
  //  CATEGORIES
  // ════════════════════════════════════════════════════════

  getCategoryIcon(name: string): string {
    const lower = name.toLowerCase();
    for (const [key, icon] of Object.entries(this.catIconMap)) {
      if (lower.includes(key)) return icon;
    }
    return '📦';
  }

  selectParentCategory(cat: Category): void {
    this.selectedParentId = cat.id;
    this.childCategories = this.categories.filter(c => c.parentId === cat.id);
    if (this.childCategories.length === 0) {
      this.form.patchValue({ categoryId: cat.id });
    } else {
      this.form.patchValue({ categoryId: null });
    }
  }

  selectChildCategory(cat: Category): void {
    this.form.patchValue({ categoryId: cat.id });
  }

  isParentSelected(cat: Category): boolean { return this.selectedParentId === cat.id; }
  isChildSelected(cat: Category): boolean  { return this.form.get('categoryId')?.value === cat.id; }

  getSelectedCategoryName(): string {
    const id = this.form.get('categoryId')?.value;
    return this.categories.find(c => c.id === id)?.name ?? '—';
  }

  getSelectedParentName(): string {
    return this.parentCategories.find(c => c.id === this.selectedParentId)?.name ?? '';
  }

  // ════════════════════════════════════════════════════════
  //  IMAGES
  // ════════════════════════════════════════════════════════

  onFileSelected(event: Event): void {
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
    const remaining = this.MAX_IMAGES - this.selectedFiles.length;
    let added = 0;
    for (let i = 0; i < files.length && added < remaining; i++) {
      const file = files[i];
      if (!file.type.match(/image\/*/)) { this.toastService.showError(`"${file.name}" nije slika.`); continue; }
      if (file.size > 10 * 1024 * 1024) { this.toastService.showError(`"${file.name}" premašuje 10MB.`); continue; }
      this.selectedFiles.push(file);
      this.previewUrls.push(URL.createObjectURL(file));
      added++;
    }
    this.form.patchValue({ images: this.previewUrls });
  }

  removeImage(index: number): void {
    URL.revokeObjectURL(this.previewUrls[index]);
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
    this.form.patchValue({ images: this.previewUrls });
  }

  setCoverImage(index: number): void {
    if (index === 0) return;
    const [f] = this.selectedFiles.splice(index, 1);
    const [u] = this.previewUrls.splice(index, 1);
    this.selectedFiles.unshift(f);
    this.previewUrls.unshift(u);
    this.form.patchValue({ images: this.previewUrls });
  }

  // ════════════════════════════════════════════════════════
  //  LOCATION
  // ════════════════════════════════════════════════════════

  onLocationChange(option: CityPickerOption | null): void {
    this.form.patchValue({ locationId: option?.locationId ?? null });
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
  //  SUBMIT
  // ════════════════════════════════════════════════════════

  onSubmit(): void {
    if (this.form.invalid || this.selectedFiles.length === 0) {
      this.form.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja.');
      return;
    }
    this.isSubmitting = true;
    this.adService.uploadImages(this.selectedFiles).pipe(
      switchMap(urls => {
        this.form.patchValue({ images: urls });
        return this.adService.createAd(this.form.value);
      })
    ).subscribe({
      next: (ad) => {
        this.isSubmitting = false;
        this.toastService.showSuccess('Oglas uspešno kreiran!');
        this.router.navigate(['/ads', ad.id]);
      },
      error: () => {
        this.isSubmitting = false;
        this.toastService.showError('Greška pri kreiranju oglasa. Pokušajte ponovo.');
      },
    });
  }

  // ════════════════════════════════════════════════════════
  //  GETTERS
  // ════════════════════════════════════════════════════════

  get titleLength(): number     { return this.form.get('title')?.value?.length ?? 0; }
  get descLength(): number      { return this.form.get('description')?.value?.length ?? 0; }
  get quantity(): number        { return this.form.get('totalQuantity')?.value ?? 1; }
  get selectedCurrency(): string { return this.form.get('currency')?.value ?? 'RSD'; }
  get selectedInterval(): string { return this.form.get('priceInterval')?.value ?? PriceInterval.PER_DAY; }
}
