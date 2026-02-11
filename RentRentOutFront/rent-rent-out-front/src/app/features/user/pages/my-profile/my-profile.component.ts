import { Component } from '@angular/core';

@Component({
  selector: 'app-my-profile',
  imports: [CommonModule],
  templateUrl: './my-profile.component.html',
  styleUrl: './my-profile.component.css'
})
export class MyProfileComponent {

  constructor(private userService: UserService,) {


  }
  ngOnInit() {
    this.userService.getMe()
  }

}
