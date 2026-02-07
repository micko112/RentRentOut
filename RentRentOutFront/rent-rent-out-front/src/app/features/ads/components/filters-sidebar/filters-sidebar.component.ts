import {Component, EventEmitter, Output} from '@angular/core';
import {Category} from '../../../../shared/models/category.model';
import {CategoryService} from '../../services/category.service';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-filters-sidebar',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './filters-sidebar.component.html',
  styleUrl: './filters-sidebar.component.css'
})

export class FiltersSidebarComponent {
  categories: Category[] = [];
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
