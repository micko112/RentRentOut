import { Component } from '@angular/core';
import {Router, RouterLink, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs';
import {User} from '../../../shared/models/user.model';
import {AuthService} from '../../../features/auth/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
      searchTerm: string = '';

      currentUser$!: Observable<User | null>;
      constructor(private router: Router,
                  private authService: AuthService,) {
      }
    ngOnInit() {
        this.currentUser$ = this.authService.currentUser$;
    }
      onSearch(){
        this.router.navigate(["/ads"], {
          queryParams: {keyword: this.searchTerm}
        })
      }
      login(){
        this.router.navigate(["/login"], {})
      }
      logout(){
        this.authService.logout();
      }
      register() {
        this.router.navigate(["/register"], {})
      }
}
