import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from "@angular/common";
import {Category} from '../../../../../shared/models/category.model';
import {CategoryService} from '../../../services/category.service';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-categories-sidebar',
    imports: [
      CommonModule
    ],
  standalone: true,
  templateUrl: './categories-sidebar.component.html',
  styleUrl: './categories-sidebar.component.css'
})
export class CategoriesSidebarComponent implements OnInit, OnDestroy {
  parentCategories: Category[] = [];
  private childrenMap = new Map<number, Category[]>();
  selectedCategoryId: number | null = null;
  @Output() categorySelected = new EventEmitter<number>();

  private destroy$ = new Subject<void>();

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    this.categoryService.getAll().pipe(takeUntil(this.destroy$)).subscribe(categories => {
      this.parentCategories = categories.filter(c => !c.parentId);
      const map = new Map<number, Category[]>();
      categories.filter(c => c.parentId).forEach(c => {
        const arr = map.get(c.parentId!) ?? [];
        arr.push(c);
        map.set(c.parentId!, arr);
      });
      this.childrenMap = map;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getChildren(parentId: number): Category[] {
    return this.childrenMap.get(parentId) ?? [];
  }

  onCategoryClick(categoryId: number | null): void {
    this.selectedCategoryId = categoryId;
    if (categoryId !== null) {
      this.categorySelected.emit(categoryId);
    }
  }
}
