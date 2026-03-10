import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../../shared/services/toast.service';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink,

  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form!: FormGroup;
  submitted = false;
  constructor(private fb: FormBuilder,
              private router: Router,
              private authService: AuthService,
              private toastService: ToastService,
    ) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  login() {
    if (this.form.invalid) {
      return;
    }
    const credentials = this.form.value;

    this.authService.login(credentials)
      .subscribe(
         {
           next: (response) => {
             this.toastService.showSucces("Uspesno ste se ulogovali!");
             localStorage.setItem('authToken', response.token);
             this.router.navigateByUrl('/');
           },
           error: (error) =>  {
             this.toastService.showError("Nije uspela prijava!")
           }

        }
      )
    this.submitted = true;

  }
}
