import {Component, OnInit} from '@angular/core';
import {Category} from '../../../shared/models/category.model';
import {CategoryService} from '../../../features/ads/services/category.service';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterModule} from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements  OnInit {
  categories: Category[] =[];
  constructor(private categoryService: CategoryService,) {
  }
  ngOnInit(): void {
    this.categoryService.getAll().subscribe(res => {
      this.categories = res;
    })
  }



}
