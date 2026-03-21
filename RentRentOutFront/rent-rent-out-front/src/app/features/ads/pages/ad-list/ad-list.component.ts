import {Component, OnInit} from '@angular/core';
import {AdCardComponent} from '../../components/ad-card/ad-card.component';
import {CommonModule} from '@angular/common';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {finalize, Observable, switchMap, tap} from 'rxjs';

import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {LocationService} from '../../services/location.service';
import {Category} from '../../../../shared/models/category.model';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AdSearchCriteria} from '../../../../shared/models/adSearchCriteria';
import {FiltersSidebarComponent} from '../../components/filters-sidebar/filters-sidebar.component';
import {
  CategoriesSidebarComponent
} from '../../components/categories-sidebar/categories-sidebar/categories-sidebar.component';
import {Location} from '../../../../shared/models/location.model';
import {SkeletonCardComponent} from '../../../../shared/components/skeleton-card/skeleton-card.component';


@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, AdCardComponent, RouterLink, FiltersSidebarComponent, CategoriesSidebarComponent, SkeletonCardComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit {

  adsPage$!: Observable<Page<AdPreview>>
  categories: Category[] = [];
  locations: Location[] = [];
  isSearchMode = false;
  currentKeyword: string = "";
  activeCategory: string = "Svi oglasi";
  totalResults: number = 0;
  isLoading = true;

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(res => {
      this.categories = res;
      this.updateActiveCategory(this.route.snapshot.queryParams['categoryId']);
    })
    this.locationService.getAll().subscribe({
      next: (locs) => this.locations = locs,
      error: () => {
        console.error('Greska pri ucitavanju lokacija.');
      }
    });
    this.adsPage$ = this.route.queryParams.pipe(
      switchMap(params => {
          this.isLoading = true;
          const categoryId = params['categoryId'] ? Number(params['categoryId']) : undefined;
          this.updateActiveCategory(categoryId);

          this.currentKeyword = params['keyword'] || "";
          this.isSearchMode = !!(
            this.currentKeyword ||
            categoryId ||
            params['locationId'] ||
            params['city'] ||
            params['minPrice'] ||
            params['maxPrice'] ||
            params['priceInterval']
          );

          const criteria: AdSearchCriteria = {
            keyword: this.currentKeyword,
            categoryId: categoryId,
            minPrice: params['minPrice'] ? Number(params['minPrice']) : undefined,
            maxPrice: params['maxPrice'] ? Number(params['maxPrice']) : undefined,
            locationId: params['locationId'] ? Number(params['locationId']) : undefined,
            city: params['city'] || undefined,
            priceInterval: params['priceInterval'] || undefined,
            page: params['page'] ? Number(params['page']) : undefined,
            size: params['size'] ? Number(params['size']) : undefined,
            sort: params['sort']
          };

          return this.adService.search(criteria).pipe(
            tap(res => this.totalResults = res.totalElements),
            finalize(() => this.isLoading = false)
          );

        }
      )
    )
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
      queryParams: {sort: sortValue},
      queryParamsHandling: 'merge'
    })
  }
  onApplyFilters(criteria: Partial<AdSearchCriteria>){
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        ...criteria,
        city: criteria.city ?? null,
        locationId: criteria.locationId ?? null,
        page: 0
      },
      queryParamsHandling: 'merge'
    })
  }

  goToPage(pageIndex: number) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {page: pageIndex},
      queryParamsHandling: 'merge'
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  getPageRange(current: number, total: number): (number | '...')[] {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    if (current <= 3) return [0, 1, 2, 3, 4, '...', total - 1];
    if (current >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1];
    return [0, '...', current - 1, current, current + 1, '...', total - 1];
  }

  asNumber(p: number | '...'): number { return p as number; }
}
