import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
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
  returnUrl: string = '/';
  constructor(private fb: FormBuilder,
              private router: Router,
              private authService: AuthService,
              private toastService: ToastService,
              private route: ActivatedRoute,
    ) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
    this.returnUrl = this.route.snapshot.queryParams['redirect'] || '/';
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
             this.toastService.showSuccess("Uspesno ste se ulogovali!");
             localStorage.setItem('authToken', response.token);
             this.router.navigateByUrl(this.returnUrl);
           },
           error: (error) =>  {
             this.toastService.showError("Nije uspela prijava!")
           }

        }
      )
    this.submitted = true;

  }
}
