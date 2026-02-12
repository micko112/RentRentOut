import {Component, OnInit} from '@angular/core';
import {AsyncPipe, CommonModule, DecimalPipe, NgIf} from '@angular/common';
import {Observable} from 'rxjs';
import {User} from '../../../../shared/models/user.model';
import {UserService} from '../../services/user.service';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../../auth/services/auth.service';
import {RouterModule, RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-profile-details',
  standalone: true,
  imports: [
    DecimalPipe,
    NgIf,
    AsyncPipe,
    CommonModule,
    RouterOutlet,
    RouterModule
  ],
  templateUrl: './profile-details.component.html',
  styleUrl: './profile-details.component.css'
})
export class ProfileDetailsComponent implements OnInit {
  user$!: Observable<User | null>;
  constructor(private userService: UserService,
              private http: HttpClient,
              private authService: AuthService,
  ) {
  }
  ngOnInit() {
    this.user$ = this.authService.currentUser$
  }
}
