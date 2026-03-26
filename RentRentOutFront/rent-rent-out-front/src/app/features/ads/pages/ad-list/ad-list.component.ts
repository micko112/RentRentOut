import { Component, OnInit, OnDestroy } from '@angular/core';
import { AdCardComponent } from '../../components/ad-card/ad-card.component';
import { CommonModule } from '@angular/common';
import { AdPreview, Page } from '../../../../shared/models/adPreview.model';
import { finalize, Subject, switchMap, takeUntil, tap, of } from 'rxjs';
import { SkeletonCardComponent } from '../../../../shared/components/skeleton-card/skeleton-card.component';

import { AdService } from '../../services/ad.service';
import { CategoryService } from '../../services/category.service';
import { LocationService } from '../../services/location.service';
import { Category } from '../../../../shared/models/category.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdSearchCriteria } from '../../../../shared/models/adSearchCriteria';
import { FiltersSidebarComponent } from '../../components/filters-sidebar/filters-sidebar.component';
import { CategoriesSidebarComponent } from '../../components/categories-sidebar/categories-sidebar/categories-sidebar.component';
import { Location } from '../../../../shared/models/location.model';


@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, AdCardComponent, RouterLink, FiltersSidebarComponent, CategoriesSidebarComponent, SkeletonCardComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit, OnDestroy {

  adsPage: Page<AdPreview> | null = null;
  categories: Category[] = [];
  locations: Location[] = [];
  isSearchMode = false;
  homeMode = false;
  currentKeyword = '';
  activeCategory = 'Svi oglasi';
  totalResults = 0;
  isLoading = true;
  currentCriteria: Partial<AdSearchCriteria> = {};

  latestLoaded = false;
  readonly skeleton9 = Array(9);
  readonly skeleton6 = Array(6);

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

  private destroy$ = new Subject<void>();
  private homeDataDestroy$ = new Subject<void>();

  constructor(
    private adService: AdService,
    private categoryService: CategoryService,
    private locationService: LocationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  private isSearchModeFromParams(params: Record<string, string>): boolean {
    return !!(
      params['keyword'] || params['categoryId'] || params['locationId'] ||
      params['city'] || params['minPrice'] || params['maxPrice'] ||
      params['priceInterval'] || params['sort']
    );
  }

  ngOnInit(): void {
    // Sinhrona inicijalizacija pre prvog CD ciklusa (sprečava NG0100)
    const snap = this.route.snapshot.queryParams;
    this.isSearchMode = this.isSearchModeFromParams(snap);
    this.homeMode = !this.isSearchMode;

    this.categoryService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.categories = res;
        this.updateActiveCategory(this.route.snapshot.queryParams['categoryId'], this.route.snapshot.queryParams['sort']);
      },
      error: () => {},
    });
    this.locationService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: locs => this.locations = locs,
      error: () => {},
    });

    // Uvek aktivna pretplata — ne zavisi od *ngIf u template-u
    this.route.queryParams.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        const categoryId = params['categoryId'] ? Number(params['categoryId']) : undefined;
        this.updateActiveCategory(categoryId, params['sort']);
        this.currentKeyword  = params['keyword'] || '';
        this.isSearchMode    = this.isSearchModeFromParams(params);
        this.homeMode        = !this.isSearchMode;

        this.currentCriteria = {
          keyword:       params['keyword']       || undefined,
          categoryId,
          minPrice:      params['minPrice']      ? Number(params['minPrice'])   : undefined,
          maxPrice:      params['maxPrice']      ? Number(params['maxPrice'])   : undefined,
          locationId:    params['locationId']    ? Number(params['locationId']) : undefined,
          city:          params['city']          || undefined,
          priceInterval: params['priceInterval'] || undefined,
        };

        if (this.homeMode) {
          this.loadHomeData();
          this.isLoading = false;
          return of(null);
        }

        this.isLoading = true;
        const criteria: AdSearchCriteria = {
          ...this.currentCriteria,
          page: params['page'] !== undefined ? Number(params['page']) : undefined,
          size: params['size']  ? Number(params['size'])  : undefined,
          sort: params['sort'],
        };

        return this.adService.search(criteria).pipe(
          tap(res => this.totalResults = res.totalElements),
          finalize(() => this.isLoading = false)
        );
      })
    ).subscribe({
      next: page => { this.adsPage = page; },
      error: () => { this.isLoading = false; },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.homeDataDestroy$.next();
    this.homeDataDestroy$.complete();
  }

  private loadHomeData(): void {
    this.homeDataDestroy$.next(); // Otkazuje prethodne nedovršene home zahteve
    this.adsPage = null;
    this.latestAds = [];
    this.latestLoaded = false;
    this.homeCategories = this.HOME_CATEGORIES.map(c => ({ ...c, ads: [], total: 0, loaded: false }));

    this.adService.search({ sort: 'id,desc', size: 9 }).pipe(
      takeUntil(this.homeDataDestroy$),
      takeUntil(this.destroy$)
    ).subscribe({
      next: page => {
        this.latestAds = page.content;
        this.latestLoaded = true;
      },
      error: () => { this.latestLoaded = true; },
    });

    this.HOME_CATEGORIES.forEach((cat, index) => {
      this.adService.search({ categoryId: cat.id, sort: 'id,desc', size: 6 }).pipe(
        takeUntil(this.homeDataDestroy$),
        takeUntil(this.destroy$)
      ).subscribe({
        next: page => {
          this.homeCategories[index] = {
            ...this.homeCategories[index],
            ads:   page.content,
            total: page.totalElements,
            loaded: true,
          };
        },
        error: () => {
          this.homeCategories[index] = { ...this.homeCategories[index], loaded: true };
        },
      });
    });
  }

  onCategoryFiltered(categoryId: number): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoryId },
    });
  }

  private updateActiveCategory(categoryId: number | undefined, sort?: string): void {
    if (!categoryId) {
      this.activeCategory = sort === 'id,desc' ? 'Najnoviji oglasi' : 'Svi oglasi';
      return;
    }
    const found = this.categories.find(c => c.id === categoryId);
    this.activeCategory = found ? found.name : 'Učitavanje...';
  }

  onSortChange(event: Event): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { sort: (event.target as HTMLSelectElement).value },
      queryParamsHandling: 'merge',
    });
  }

  onApplyFilters(criteria: Partial<AdSearchCriteria>): void {
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
      },
    });
  }

  goToPage(pageIndex: number): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: pageIndex },
      queryParamsHandling: 'merge',
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
