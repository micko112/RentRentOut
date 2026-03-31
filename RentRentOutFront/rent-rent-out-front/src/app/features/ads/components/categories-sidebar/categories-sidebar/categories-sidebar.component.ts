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
  private grandchildrenMap = new Map<number, Category[]>();
  expandedLevel2Ids = new Set<number>();
  selectedCategoryId: number | null = null;
  @Output() categorySelected = new EventEmitter<number>();

  private destroy$ = new Subject<void>();

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    this.categoryService.getAll().pipe(takeUntil(this.destroy$)).subscribe(categories => {
      this.parentCategories = categories.filter(c => !c.parentId);
      const level1Ids = new Set(this.parentCategories.map(c => c.id));

      const childrenMap = new Map<number, Category[]>();
      const grandchildrenMap = new Map<number, Category[]>();

      categories.filter(c => c.parentId).forEach(c => {
        if (level1Ids.has(c.parentId!)) {
          const arr = childrenMap.get(c.parentId!) ?? [];
          arr.push(c);
          childrenMap.set(c.parentId!, arr);
        } else {
          const arr = grandchildrenMap.get(c.parentId!) ?? [];
          arr.push(c);
          grandchildrenMap.set(c.parentId!, arr);
        }
      });

      this.childrenMap = childrenMap;
      this.grandchildrenMap = grandchildrenMap;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getChildren(parentId: number): Category[] {
    return this.childrenMap.get(parentId) ?? [];
  }

  getGrandchildren(parentId: number): Category[] {
    return this.grandchildrenMap.get(parentId) ?? [];
  }

  hasGrandchildren(parentId: number): boolean {
    return (this.grandchildrenMap.get(parentId)?.length ?? 0) > 0;
  }

  toggleExpand(id: number): void {
    if (this.expandedLevel2Ids.has(id)) {
      this.expandedLevel2Ids.delete(id);
    } else {
      this.expandedLevel2Ids.add(id);
    }
  }

  onCategoryClick(categoryId: number | null): void {
    this.selectedCategoryId = categoryId;
    if (categoryId !== null) {
      this.categorySelected.emit(categoryId);
    }
  }
}
