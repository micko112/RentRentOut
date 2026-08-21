import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslateModule],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css'
})
export class VerifyEmailComponent implements OnInit {
  status: 'loading' | 'success' | 'error' = 'loading';
  message = '';

  constructor(private route: ActivatedRoute,
              private authService: AuthService,
              private router: Router,
              private translate: TranslateService) {
    this.message = this.translate.instant('verifyEmail.loading');
  }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status = 'error';
      this.message = this.translate.instant('verifyEmail.missing_token');
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: () => {
        this.status = 'success';
        setTimeout(() => this.router.navigate(['/']), 1500);
      },
      error: (err) => {
        this.status = 'error';
        this.message = err?.error?.message || err?.error || this.translate.instant('verifyEmail.error_generic');
      }
    });
  }
}
