import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {LocationService} from '../../services/location.service';
import {Category} from '../../../../shared/models/category.model';
import {PriceInterval, PriceIntervalLabels} from '../../../../shared/models/price-interval.enum';
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
  form!: FormGroup;
  categories: Category[] = [];
  priceIntervals = Object.values(PriceInterval);
  priceLabels = PriceIntervalLabels;

  locations: Location[] = [];

  selectedFiles: File[] = [];
  previewUrl: string[] = [];
  existingImages: string[] = [];

  adId!: number;
  currentAd!: Ad;
  isLoading = true;

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private fb: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private toastService: ToastService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      price: [null, [Validators.required, Validators.min(1)]],
      currency: ['', [Validators.required, Validators.min(3)]],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      images: [[], Validators.required]
    });

    const paramId = this.route.snapshot.paramMap.get('id');
    this.adId = paramId ? Number(paramId) : NaN;

    if (!this.adId || Number.isNaN(this.adId)) {
      this.toastService.showError('Neispravan ID oglasa.');
      this.router.navigate(['/user/me/ads']);
      return;
    }

    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
      },
      error: () => {
        this.toastService.showError('Greska pri ucitavanju kategorija.');
      }
    });
    this.locationService.getAll().subscribe({
      next: (locs) => {
        this.locations = locs;
      },
      error: () => {
        this.toastService.showError('Greska pri ucitavanju lokacija.');
      }
    });

    this.adService.getAdById(this.adId).subscribe({
      next: (ad) => {
        this.currentAd = ad;
        this.existingImages = [...(ad.images || [])];
        this.ensureLocationInList(ad.location);

        this.form.patchValue({
          title: ad.title,
          description: ad.description,
          price: ad.price,
          currency: ad.currency,
          priceInterval: ad.priceInterval,
          categoryId: ad.category?.id ?? null,
          locationId: ad.location?.id ?? null,
          totalQuantity: ad.totalQuantity,
          images: this.existingImages
        });

        this.isLoading = false;
      },
      error: () => {
        this.toastService.showError('Ne mogu da ucitam oglas.');
        this.router.navigate(['/user/me/ads']);
      }
    });
  }

  private ensureLocationInList(location?: Location | null): void {
    if (!location) return;
    const exists = this.locations.some(loc => loc.id === location.id);
    if (!exists) {
      this.locations = [...this.locations, location];
    }
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    if (!files) return;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (!file.type.match(/image\/*/)) continue;
      this.selectedFiles.push(file);
      this.previewUrl.push(URL.createObjectURL(file));
    }
    this.syncImagesControl();
  }

  removeExistingImage(index: number): void {
    this.existingImages.splice(index, 1);
    this.syncImagesControl();
  }

  removeNewImage(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.previewUrl.splice(index, 1);
    this.syncImagesControl();
  }

  private syncImagesControl(): void {
    const combined = [...this.existingImages, ...this.previewUrl];
    this.form.patchValue({images: combined});
    this.form.get('images')?.updateValueAndValidity();
  }

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
