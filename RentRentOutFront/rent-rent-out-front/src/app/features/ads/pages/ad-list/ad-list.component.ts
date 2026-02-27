import {Component, OnInit} from '@angular/core';
import {AdCardComponent} from '../../components/ad-card/ad-card.component';
import {CommonModule} from '@angular/common';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {Observable, switchMap, tap} from 'rxjs';

import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {Category} from '../../../../shared/models/category.model';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AdSearchCriteria} from '../../../../shared/models/adSearchCriteria';
import {FiltersSidebarComponent} from '../../components/filters-sidebar/filters-sidebar.component';
import {
  CategoriesSidebarComponent
} from '../../components/categories-sidebar/categories-sidebar/categories-sidebar.component';


@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, AdCardComponent, RouterLink, FiltersSidebarComponent, CategoriesSidebarComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit {

  adsPage$!: Observable<Page<AdPreview>>
  categories: Category[] = [];
  isSearchMode = false;
  currentKeyword: string = "";
  activeCategory: string = "Svi oglasi";
  totalResults: number = 0;

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(res => {
      console.log('Kategorije sa backenda: ', res);
      this.categories = res;
      this.updateActiveCategory(this.route.snapshot.queryParams['categoryId']);
    })
    this.adsPage$ = this.route.queryParams.pipe(
      switchMap(params => {
          const categoryId = params['categoryId'] ? Number(params['categoryId']) : undefined;
          this.updateActiveCategory(categoryId);

          this.currentKeyword = params['keyword'] || "";
          this.isSearchMode = !!(this.currentKeyword || categoryId)

          const criteria: AdSearchCriteria = {
            keyword: this.currentKeyword,
            categoryId: categoryId,
            minPrice: params['minPrice'] ? Number(params['minPrice']) : undefined,
            maxPrice: params['maxPrice'] ? Number(params['maxPrice']) : undefined,
            locationId: params['locationId'] ? Number(params['locationId']) : undefined,
            page: params['page'] ? Number(params['page']) : undefined,
            size: params['size'] ? Number(params['size']) : undefined,
            sort: params['sort']
          };

          return this.adService.search(criteria).pipe(
            tap(res => this.totalResults = res.totalElements
            )
          );

        }
      )
    )
    this.adsPage$.subscribe(res => {
      console.log('ADS PAGE:', res);
      console.log()
    });
  }

  onCategoryFiltered(categoryId: number): void {
    this.router.navigate([],
      {
        relativeTo: this.route,
        queryParams: {categoryId: categoryId},
        queryParamsHandling: 'merge'
      })
  };

  private updateActiveCategory(categoryId: number | undefined): void {
    if (!categoryId) {
      this.activeCategory = "Svi oglasi";
      return;
    }
    const foundCategory = this.categories.find(c => c.id === categoryId);
    if (foundCategory) {
      this.activeCategory = foundCategory.name;
    } else {
      this.activeCategory = "Učitavanje";
    }
  }
  onSortChange(event: any) {
    const sortValue = event.target.value;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {query: sortValue},
      queryParamsHandling: 'merge'
    })
  }
  onApplyFilters(criteria: Partial<AdSearchCriteria>){
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        ...criteria,
        page: 0
      },
      queryParamsHandling: 'merge'
    })
  }
}
