import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdService} from '../../services/ad.service';
import {Category} from '../../../../shared/models/category.model';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {PriceInterval, PriceIntervalLabels} from '../../../../shared/models/price-interval.enum';
import {CategoryService} from '../../services/category.service';
import {LocationService} from '../../services/location.service';
import {Router, RouterLink} from '@angular/router';
import {switchMap} from 'rxjs';
import {ToastService} from '../../../../shared/services/toast.service';
import {Location} from '../../../../shared/models/location.model';

@Component({
  selector: 'app-create-ad',
  imports: [CommonModule, ReactiveFormsModule, RouterLink,],
  standalone: true,
  templateUrl: './create-ad.component.html',
  styleUrl: './create-ad.component.css'
})
export class CreateAdComponent implements OnInit {
  selectedFiles: File[] = [];

  previewUrl: string[] = [];

  categories: Category[] = [];
  form!: FormGroup;
  priceIntervals = Object.values(PriceInterval);
  priceLabels = PriceIntervalLabels;
  locations: Location[] = [];

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private fb: FormBuilder,
              private router: Router,
              private toastService: ToastService,) {
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(category => {
      this.categories = category;
      console.log('CreateAdComponent je inicijalizovan!');
    });
    this.locationService.getAll().subscribe({
      next: (locs) => this.locations = locs,
      error: () => this.toastService.showError('Greska pri ucitavanju lokacija.')
    });

    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      price: [null, [Validators.required, Validators.min(1)]],
      currency: ['', [Validators.required, Validators.min(3)]],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      images: [['https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?q=80&w=1000&auto=format&fit=crop']]
    })
  }

  onSubmit() {
    if (this.form.invalid || this.selectedFiles.length === 0) {
      alert('Molimo popunite sva polja i izaberite barem jednu sliku.');
      this.form.markAsTouched();
      return;
    }
    this.adService.uploadImages(this.selectedFiles).pipe(
      switchMap(uploadedImagesUrls => {
        this.form.patchValue({images: uploadedImagesUrls});
        console.log(this.selectedFiles);
        return this.adService.createAd(this.form.value);
      })
    ).subscribe({
      next: (newAd) => {
        this.toastService.showSuccess('Oglas uspesno kreiran');
        this.router.navigate(['/ads', newAd.id]);
      },
      error: (error) => {
        console.error('Greska:', error);
        this.toastService.showError('Doslo je do greske prilikom kreiranja oglasa.');

      }
    })
  }

  onFileSelected(event: any){
    const files: FileList = event.target.files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.match(/image\/*/)) continue;

        this.selectedFiles.push(file);

        this.previewUrl.push(URL.createObjectURL(file));

        this.form.patchValue({ images: this.previewUrl });
        this.form.get('images')?.updateValueAndValidity();
      }
    }
  }

  removeImage(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.previewUrl.splice(index, 1);
  }
}