import { Component } from '@angular/core';
import {AsyncPipe, DecimalPipe, NgIf} from '@angular/common';
import {Observable} from 'rxjs';
import {User} from '../../../../shared/models/user.model';
import {UserService} from '../../services/user.service';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../../auth/services/auth.service';

@Component({
  selector: 'app-profile-details',
  imports: [
    DecimalPipe,
    NgIf,
    AsyncPipe
  ],
  templateUrl: './profile-details.component.html',
  styleUrl: './profile-details.component.css'
})
export class ProfileDetailsComponent {
  user$!: Observable<User>;
  constructor(private userService: UserService,
              private http: HttpClient,
              private authService: AuthService,
  ) {


  }
}
