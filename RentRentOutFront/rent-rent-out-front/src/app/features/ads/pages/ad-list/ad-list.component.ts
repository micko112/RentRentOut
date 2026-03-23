import {Component, OnInit} from '@angular/core';
import {AdCardComponent} from '../../components/ad-card/ad-card.component';
import {CommonModule} from '@angular/common';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {finalize, Observable, of, switchMap, tap} from 'rxjs';

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
  locations: Location[] = [];
  isSearchMode = false;
  homeMode = false;
  currentKeyword: string = "";
  activeCategory: string = "Svi oglasi";
  totalResults: number = 0;
  isLoading = true;
  currentCriteria: Partial<AdSearchCriteria> = {};

  latestAds: AdPreview[] = [];
  homeCategories: Array<{
    id: number;
    displayName: string;
    icon: string;
    ads: AdPreview[];
    total: number;
    loaded: boolean;
  }> = [];

  private readonly HOME_CATEGORIES = [
    { id: 200, displayName: 'Tehnologija i uređaji',        icon: 'devices'       },
    { id: 300, displayName: 'Oprema za film i fotografiju', icon: 'photo_camera'  },
    { id: 100, displayName: 'Alati i oruđa',                icon: 'construction'  },
    { id: 600, displayName: 'Događaji i zurke',             icon: 'celebration'   },
    { id: 700, displayName: 'Prevoz i oprema za prirodu',   icon: 'explore'       },
  ];

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  private isSearchModeFromParams(params: Record<string, string>): boolean {
    return !!(
      params['keyword'] || params['categoryId'] || params['locationId'] ||
      params['city'] || params['minPrice'] || params['maxPrice'] ||
      params['priceInterval'] || params['sort']
    );
  }

  ngOnInit(): void {
    // Postavljamo inicijalne vrednosti iz snapshot-a SINHRONO pre prvog CD ciklusa
    // (sprečava NG0100 koji nastaje kad async pipe detektuje promenu tokom prvog check-a)
    const initialParams = this.route.snapshot.queryParams;
    this.isSearchMode = this.isSearchModeFromParams(initialParams);
    this.homeMode = !this.isSearchMode;
    if (this.homeMode) {
      this.loadHomeData();
    }

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
          this.isSearchMode = this.isSearchModeFromParams(params);
          this.homeMode = !this.isSearchMode;

          if (this.homeMode) {
            this.loadHomeData();
            this.isLoading = false;
            return of({ content: [] as AdPreview[], totalElements: 0, totalPages: 0, number: 0, first: true, last: true, size: 0 });
          }

          const criteria: AdSearchCriteria = {
            keyword:       this.currentKeyword,
            categoryId:    categoryId,
            minPrice:      params['minPrice']      ? Number(params['minPrice'])   : undefined,
            maxPrice:      params['maxPrice']      ? Number(params['maxPrice'])   : undefined,
            locationId:    params['locationId']    ? Number(params['locationId']) : undefined,
            city:          params['city']          || undefined,
            priceInterval: params['priceInterval'] || undefined,
            page:          params['page'] !== undefined ? Number(params['page'])  : undefined,
            size:          params['size']          ? Number(params['size'])       : undefined,
            sort:          params['sort']
          };

          this.currentCriteria = {
            keyword:       criteria.keyword,
            categoryId:    criteria.categoryId,
            minPrice:      criteria.minPrice,
            maxPrice:      criteria.maxPrice,
            locationId:    criteria.locationId,
            city:          criteria.city,
            priceInterval: criteria.priceInterval,
          };

          return this.adService.search(criteria).pipe(
            tap(res => this.totalResults = res.totalElements),
            finalize(() => this.isLoading = false)
          );

        }
      )
    )
  }

  private loadHomeData(): void {
    this.latestAds = [];
    this.homeCategories = this.HOME_CATEGORIES.map(c => ({ ...c, ads: [], total: 0, loaded: false }));

    this.adService.search({ sort: 'id,desc', size: 9 }).subscribe(page => {
      this.latestAds = page.content;
    });

    this.HOME_CATEGORIES.forEach((cat, index) => {
      this.adService.search({ categoryId: cat.id, sort: 'id,desc', size: 6 }).subscribe(page => {
        this.homeCategories[index] = {
          ...this.homeCategories[index],
          ads: page.content,
          total: page.totalElements,
          loaded: true
        };
      });
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
      queryParams: {sort: sortValue},
      queryParamsHandling: 'merge'
    })
  }
  onApplyFilters(criteria: Partial<AdSearchCriteria>){
    const currentSort = this.route.snapshot.queryParams['sort'] || null;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        keyword:       criteria.keyword       || null,
        categoryId:    criteria.categoryId    || null,
        city:          criteria.city          || null,
        locationId:    criteria.locationId    || null,
        minPrice:      criteria.minPrice      || null,
        maxPrice:      criteria.maxPrice      || null,
        priceInterval: criteria.priceInterval || null,
        sort:          currentSort,
        page:          0,
      }
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
