import {Component, OnInit} from '@angular/core';
import {AdCardComponent} from '../../components/ad-card/ad-card.component';
import {CommonModule} from '@angular/common';
import {AdPreview, Page} from '../../../../shared/models/adPreview';
import {Observable, switchMap} from 'rxjs';

import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {Category} from '../../../../shared/models/category.model';
import {ActivatedRoute} from '@angular/router';
import {AdSearchCriteria} from '../../../../shared/models/adSearchCriteria';



@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, AdCardComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit {

  adsPage$!: Observable<Page<AdPreview>>
  categories: Category[] = [];

  constructor(private adService: AdService, private categoryService: CategoryService, private route: ActivatedRoute,) {
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(res => {
      console.log('Kategorije sa backenda: ', res);
      this.categories = res;
    })
    this.adsPage$ = this.route.queryParams.pipe(
      switchMap(params => {
        const criteria: AdSearchCriteria = {
          keyword: params['keyword'],
          categoryId: params['categoryId'] ? Number(params['categoryId']) : undefined,
          minPrice: params['minPrice'] ? Number(params['minPrice']) : undefined,
          maxPrice: params['maxPrice'] ? Number(params['maxPrice']) : undefined,
          locationId: params['locationId'] ? Number(params['locationId']) : undefined,
          page: params['page'] ? Number(params['page']) : undefined,
          size: params['size'] ? Number(params['size']) : undefined,
          sort: params['sort'] ? Number(params['sort']) : undefined
        };
        return this.adService.search(criteria);

      })
    )

    this.adsPage$.subscribe(res => {
      console.log('ADS PAGE:', res);
    });

  }
}
