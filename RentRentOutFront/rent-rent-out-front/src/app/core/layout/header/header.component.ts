import { Component } from '@angular/core';
import {Router, RouterLink, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
      searchTerm: string = '';
      constructor(private router: Router) {
      }

      onSearch(){
        this.router.navigate(["/ads"], {
          queryParams: {keyword: this.searchTerm}
        })
      }
}
