import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { AdCardComponent } from '../../components/ad-card/ad-card.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
import { SeoService } from '../../../../core/services/seo.service';
import { MobileFilterService } from '../../../../core/services/mobile-filter.service';

@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AdCardComponent, RouterLink, FiltersSidebarComponent, CategoriesSidebarComponent, SkeletonCardComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit, OnDestroy {

  adsPage: Page<AdPreview> | null = null;
  categories: Category[] = [];
  locations: Location[] = [];
  isSearchMode = false;
  homeMode = false;
  showMobileFilters = false;
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
    { id: 200,   displayName: 'Elektronika',         icon: 'devices'       },
    { id: 300,   displayName: 'Foto i video',        icon: 'photo_camera'  },
    { id: 100,   displayName: 'Alati i oprema',      icon: 'construction'  },
    { id: 600,   displayName: 'Događaji i zurke',    icon: 'celebration'   },
    { id: 700,   displayName: 'Sport i rekreacija',  icon: 'sports'        },
    { id: 10000, displayName: 'Garderoba',           icon: 'checkroom'     },
  ];

  readonly mobileCategoryStrip = [
    { id: 200,   shortName: 'Elektronika', icon: 'devices'       },
    { id: 300,   shortName: 'Foto',        icon: 'photo_camera'  },
    { id: 100,   shortName: 'Alati',       icon: 'construction'  },
    { id: 600,   shortName: 'Događaji',    icon: 'celebration'   },
    { id: 700,   shortName: 'Sport',       icon: 'sports'        },
    { id: 10000, shortName: 'Garderoba',   icon: 'checkroom'     },
  ];

  readonly popularCities = [
    'Beograd', 'Novi Beograd', 'Zemun', 'Voždovac', 'Zvezdara',
    'Novi Sad', 'Niš', 'Kragujevac', 'Subotica', 'Pančevo',
  ];

  /* ─── Mobile filter state ─── */
  mobileFilterView: 'main' | 'kategorije' | 'mesto' | 'cena' = 'main';
  pendingMobileFilters: Partial<AdSearchCriteria> = {};
  categorySearch = '';
  citySearch = '';
  mobileMinPrice: number | null = null;
  mobileMaxPrice: number | null = null;
  mobileCurrency = 'RSD';
  mobileFilterCategoryStack: { id: number; name: string }[] = [];

  private destroy$ = new Subject<void>();
  private homeDataDestroy$ = new Subject<void>();

  private seoService = inject(SeoService);
  private mobileFilterService = inject(MobileFilterService);

  constructor(
    private adService: AdService,
    private categoryService: CategoryService,
    private locationService: LocationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  /* ─── Getteri ─── */

  get mobileView(): boolean {
    return typeof window !== 'undefined' && window.innerWidth <= 768;
  }

  get filterSubviewTitle(): string {
    if (this.mobileFilterView === 'kategorije') {
      return this.mobileFilterCategoryStack.length > 0
        ? this.mobileFilterCategoryStack[this.mobileFilterCategoryStack.length - 1].name
        : 'Kategorije';
    }
    const titles: Record<string, string> = { mesto: 'Mesto', cena: 'Cena' };
    return titles[this.mobileFilterView] ?? 'Filteri';
  }

  get allCities(): string[] {
    return [...new Set(this.locations.map(l => l.city))].sort();
  }

  get currentCategoryList(): Category[] {
    if (this.mobileFilterCategoryStack.length === 0) {
      const q = this.categorySearch.toLowerCase().trim();
      const top = this.categories.filter(c => !c.parentId);
      if (!q) return top;
      return this.categories.filter(c => c.name.toLowerCase().includes(q));
    }
    const parentId = this.mobileFilterCategoryStack[this.mobileFilterCategoryStack.length - 1].id;
    return this.categories.filter(c => c.parentId === parentId);
  }

  categoryHasChildren(catId: number): boolean {
    return this.categories.some(c => c.parentId === catId);
  }

  get filteredMobileCities(): string[] {
    const q = this.citySearch.toLowerCase().trim();
    if (!q) return this.allCities;
    return this.allCities.filter(c => c.toLowerCase().includes(q));
  }

  getCategoryName(id: number | undefined): string {
    if (!id) return '';
    return this.categories.find(c => c.id === id)?.name ?? '';
  }

  /* ─── Mobile filter akції ─── */

  onDrawerBack(): void {
    if (this.mobileFilterView === 'kategorije') {
      if (this.mobileFilterCategoryStack.length > 0) {
        this.mobileFilterCategoryStack.pop();
        this.categorySearch = '';
      } else {
        this.mobileFilterView = 'main';
      }
    } else if (this.mobileFilterView !== 'main') {
      this.mobileFilterView = 'main';
    } else {
      this.closeMobileFilters();
    }
  }

  onMobileCategoryClick(cat: Category): void {
    if (this.categoryHasChildren(cat.id)) {
      this.mobileFilterCategoryStack = [...this.mobileFilterCategoryStack, { id: cat.id, name: cat.name }];
      this.categorySearch = '';
    } else {
      this.pendingMobileFilters = { ...this.pendingMobileFilters, categoryId: cat.id };
      this.mobileFilterCategoryStack = [];
      this.mobileFilterView = 'main';
      this.categorySearch = '';
    }
  }

  selectMobileCategoryAll(): void {
    this.pendingMobileFilters = { ...this.pendingMobileFilters, categoryId: undefined };
    this.mobileFilterCategoryStack = [];
    this.mobileFilterView = 'main';
    this.categorySearch = '';
  }

  selectMobileCity(city: string | null): void {
    this.pendingMobileFilters = { ...this.pendingMobileFilters, city: city ?? undefined };
    this.mobileFilterView = 'main';
    this.citySearch = '';
  }

  applyMobileFilters(): void {
    if (this.mobileFilterView === 'cena') {
      this.pendingMobileFilters = {
        ...this.pendingMobileFilters,
        minPrice:      this.mobileMinPrice ?? undefined,
        maxPrice:      this.mobileMaxPrice ?? undefined,
        priceInterval: this.mobileCurrency === 'EUR' ? 'EUR' : undefined,
      };
    }
    this.onApplyFilters(this.pendingMobileFilters);
  }

  /* ─── Lifecycle ─── */

  private isSearchModeFromParams(params: Record<string, string>): boolean {
    return !!(
      params['keyword'] || params['categoryId'] || params['locationId'] ||
      params['city'] || params['minPrice'] || params['maxPrice'] ||
      params['priceInterval'] || params['sort']
    );
  }

  ngOnInit(): void {
    const snap = this.route.snapshot.queryParams;
    this.isSearchMode = this.isSearchModeFromParams(snap);
    this.homeMode = !this.isSearchMode;

    this.mobileFilterService.toggle$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.toggleMobileFilters();
    });

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
          this.seoService.setHomePage();
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
          tap(res => {
            this.totalResults = res.totalElements;
            this.seoService.setSearchPage({
              categoryName:  this.activeCategory,
              keyword:       params['keyword'] || undefined,
              city:          params['city']    || undefined,
              totalResults:  res.totalElements,
            });
          }),
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
    this.seoService.reset();
    if (typeof document !== 'undefined') {
      document.body.classList.remove('drawer-open');
    }
  }

  private loadHomeData(): void {
    this.homeDataDestroy$.next();
    this.adsPage = null;
    this.latestAds = [];
    this.latestLoaded = false;
    this.homeCategories = this.HOME_CATEGORIES.map(c => ({ ...c, ads: [], total: 0, loaded: false }));

    this.adService.search({ sort: 'id,desc', size: 9, promoSort: false }).pipe(
      takeUntil(this.homeDataDestroy$),
      takeUntil(this.destroy$)
    ).subscribe({
      next: page => { this.latestAds = page.content; this.latestLoaded = true; },
      error: () => { this.latestLoaded = true; },
    });

    this.HOME_CATEGORIES.forEach((cat, index) => {
      this.adService.search({ categoryId: cat.id, sort: 'id,desc', size: 6, promoSort: false }).pipe(
        takeUntil(this.homeDataDestroy$),
        takeUntil(this.destroy$)
      ).subscribe({
        next: page => {
          this.homeCategories[index] = { ...this.homeCategories[index], ads: page.content, total: page.totalElements, loaded: true };
        },
        error: () => {
          this.homeCategories[index] = { ...this.homeCategories[index], loaded: true };
        },
      });
    });
  }

  toggleMobileFilters(): void {
    if (!this.showMobileFilters) {
      this.pendingMobileFilters = { ...this.currentCriteria };
      this.mobileMinPrice = this.currentCriteria.minPrice ?? null;
      this.mobileMaxPrice = this.currentCriteria.maxPrice ?? null;
      this.mobileFilterView = 'main';
      this.categorySearch = '';
      this.citySearch = '';
      this.mobileFilterCategoryStack = [];
    }
    this.showMobileFilters = !this.showMobileFilters;
    if (typeof document !== 'undefined') {
      document.body.classList.toggle('drawer-open', this.showMobileFilters);
    }
  }

  closeMobileFilters(): void {
    this.showMobileFilters = false;
    this.mobileFilterView = 'main';
    this.mobileFilterCategoryStack = [];
    if (typeof document !== 'undefined') {
      document.body.classList.remove('drawer-open');
    }
  }

  resetMobileFilters(): void {
    this.pendingMobileFilters = {};
    this.mobileMinPrice = null;
    this.mobileMaxPrice = null;
    this.mobileCurrency = 'RSD';
    this.closeMobileFilters();
    this.router.navigate(['/ads']);
  }

  onCategoryFiltered(categoryId: number): void {
    this.closeMobileFilters();
    this.router.navigate([], { relativeTo: this.route, queryParams: { categoryId } });
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
    this.closeMobileFilters();
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
    if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  getPageRange(current: number, total: number): (number | '...')[] {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    if (current <= 3) return [0, 1, 2, 3, 4, '...', total - 1];
    if (current >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1];
    return [0, '...', current - 1, current, current + 1, '...', total - 1];
  }

  asNumber(p: number | '...'): number { return p as number; }

  trackByAdId(_: number, ad: AdPreview): number { return ad.id; }
  trackByCatId(_: number, cat: { id: number }): number { return cat.id; }
  trackByPage(_: number, p: number | '...'): number | string { return p; }
}
