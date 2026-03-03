import {Component, OnInit} from '@angular/core';
import {NgIf,AsyncPipe, CommonModule,} from '@angular/common';
import {User} from '../../../../shared/models/user.model';
import {Observable} from 'rxjs';
import {Ad} from '../../../../shared/models/ad.model';
import {UserService} from '../../services/user.service';
import {AdService} from '../../../ads/services/ad.service';
import {ActivatedRoute, Router} from '@angular/router';
import {AdPreview, Page} from '../../../../shared/models/adPreview.model';
import {AdCardComponent} from '../../../ads/components/ad-card/ad-card.component';

@Component({
  selector: 'app-my-ads',
  imports: [
    NgIf,
    AdCardComponent,
    CommonModule
  ],
  templateUrl: './my-ads.component.html',
  styleUrl: './my-ads.component.css'
})
export class MyAdsComponent implements  OnInit {
  user$!: Observable<User>;
  ads$!: Observable<Page<AdPreview>>;
  constructor(private userService: UserService,
              private adService: AdService,
              private route: ActivatedRoute,
              private router: Router,) {}

  ngOnInit() {

    this.user$ = this.userService.getMe();

    this.ads$ = this.adService.getMyAds();
  }
  onDelete(adId: number) {
    if (confirm('Da li ste sigurni da želite da obrišete ovaj oglas?')) {
      this.adService.deleteAd(adId).subscribe({
        next: () => {
          // Osveži listu nakon brisanja
          this.ads$ = this.adService.getMyAds();
          // TODO: Dodaj neki Toast/Snack poruku za uspeh
        },
        error: (err) => alert('Greška pri brisanju oglasa.')
      });
    }
  }

  // 2. Metoda za navigaciju na edit stranicu
  onEdit(adId: number) {
    this.router.navigate(['/ads/edit', adId]);
  }
}
