import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css'
})
export class VerifyEmailComponent implements OnInit {
  status: 'loading' | 'success' | 'error' = 'loading';
  message = 'Verifikacija u toku...';

  constructor(private route: ActivatedRoute,
              private authService: AuthService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status = 'error';
      this.message = 'Nedostaje token za verifikaciju.';
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: () => {
        this.status = 'success';
        this.message = 'Email je uspesno verifikovan. Mozete da se prijavite.';
      },
      error: (err) => {
        this.status = 'error';
        this.message = err?.error?.message || 'Verifikacija nije uspela. Pokusajte ponovo.';
      }
    });
  }
}