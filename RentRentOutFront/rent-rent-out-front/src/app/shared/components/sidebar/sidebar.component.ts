import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {Subscription} from 'rxjs';
import {AuthService} from '../../../features/auth/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit, OnDestroy {
  userName = 'Korisnik';
  userEmail = '';
  private userSub?: Subscription;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      if (user) {
        const fullName = `${user.firstname || ''} ${user.lastname || ''}`.trim();
        this.userName = fullName || 'Korisnik';
        this.userEmail = user.email || '';
      } else {
        this.userName = 'Korisnik';
        this.userEmail = '';
        console.log(this.userName, "NIGGER");
      }
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }
}
