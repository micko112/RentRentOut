import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdService} from '../../services/ad.service';
import {Category} from '../../../../shared/models/category.model';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {PriceInterval, PriceIntervalLabels} from '../../../../shared/models/price-interval.enum';
import {CategoryService} from '../../services/category.service';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-create-ad',
  imports: [CommonModule, ReactiveFormsModule, RouterLink,],
  standalone: true,
  templateUrl: './create-ad.component.html',
  styleUrl: './create-ad.component.css'
})
export class CreateAdComponent implements OnInit {
  categories: Category[] = [];
  form!: FormGroup;
  priceIntervals = Object.values(PriceInterval);
  priceLabels = PriceIntervalLabels;
  locations = [
    {id: 1, name: 'Beograd'},
    {id: 2, name: 'Niš'},
    {id: 3, name: 'Novi Sad'}
  ];

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private fb: FormBuilder,
              private router: Router,) {
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(category => {
      this.categories = category
      console.log("CreateAdComponent je inicijalizovan!");
    });

    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      price: [null, [Validators.required, Validators.min(1)]],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      images: [['https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?q=80&w=1000&auto=format&fit=crop']]
    })
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAsTouched();
      return;
    }
    this.adService.createAd(this.form.value).subscribe({
      next: (newAd) => {
        console.log("Oglas kreiran", newAd);
        this.router.navigate(['/ads', newAd.id]);
      },
      error: (error) => {
        console.error("Greska:", error);
        alert('Došlo je do greške prilikom kreiranja oglasa.');
      }
    })
  }
}
