import {Component, OnInit} from '@angular/core';
import {AsyncPipe, CommonModule, NgIf} from '@angular/common';
import {Observable, switchMap} from 'rxjs';
import {User} from '../../../../shared/models/user.model';
import {UserService} from '../../services/user.service';
import {ActivatedRoute, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../../auth/services/auth.service';

@Component({
  selector: 'app-my-profile',
  imports: [CommonModule, NgIf, AsyncPipe, RouterLinkActive, RouterLink],
  standalone: true,
  templateUrl: './my-profile.component.html',
  styleUrl: './my-profile.component.css'
})
export class MyProfileComponent implements OnInit {
  user$!: Observable<User>;
  constructor(private userService: UserService,
              private http: HttpClient,
              private authService: AuthService,
              ) {


  }
  ngOnInit() {
    this.user$ = this.userService.getMe();
  }

  logout() {
    this.authService.logout();
  }


}
