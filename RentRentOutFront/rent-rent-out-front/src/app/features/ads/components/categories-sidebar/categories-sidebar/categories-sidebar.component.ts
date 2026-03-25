import {Component, EventEmitter, Output} from '@angular/core';
import {CommonModule} from "@angular/common";
import {Category} from '../../../../../shared/models/category.model';
import {CategoryService} from '../../../services/category.service';

@Component({
  selector: 'app-categories-sidebar',
    imports: [
      CommonModule
    ],
  standalone: true,
  templateUrl: './categories-sidebar.component.html',
  styleUrl: './categories-sidebar.component.css'
})
export class CategoriesSidebarComponent {
  categories: Category[] = [];
  selectedCategoryId: number =0;
  @Output() categorySelected = new EventEmitter<number>();
  constructor(private categoryService: CategoryService) {
  }
  ngOnInit() {
    this.categoryService.getAll().subscribe(category => this.categories = category);
  }
  onCategoryClick(categoryId: number) {
    this.categorySelected.emit(categoryId);

  }
}
