import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Ad} from '../../../../shared/models/ad.model';
import {CommonModule} from '@angular/common';
import {map, Observable, switchMap, tap} from 'rxjs';
import {AdService} from '../../services/ad.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {ToastService} from '../../../../shared/services/toast.service';
import {InitialsPipe} from '../../../../shared/pipes/initials.pipe';
import {ReviewCardComponent} from '../../../review/components/review-card/review-card.component';
import {Review} from '../../../../shared/models/review';
import {ReviewService} from '../../../review/services/review.service';
import {UserService} from '../../../user/services/user.service';
import {AuthService} from '../../../auth/services/auth.service';
import {RentalCalendarComponent} from '../../components/rental-calendar/rental-calendar.component';
import {ReportModalComponent} from '../../components/report-modal/report-modal.component';
import {Title, Meta} from '@angular/platform-browser';

@Component({
  selector: 'app-ad-details',
  standalone: true,
  imports: [CommonModule, RouterLink, InitialsPipe, ReviewCardComponent, RentalCalendarComponent, ReportModalComponent],
  templateUrl: './ad-details.component.html',
  styleUrl: './ad-details.component.css'
})
export class AdDetailsComponent implements OnInit, OnDestroy {
  ad$!: Observable<Ad>;
  selectedImageUrl: string | null = null;

  isOverlayOpen: boolean = false;
  currentOverlayIndex: number = 0;

  isMyAd: boolean = false;
  currentAd!: Ad;
  blockedIntervals: { start: Date, end: Date }[] = [];

  realPhoneNumber: string | null = null;
  isLoadingPhone: boolean = false;

  latestReviews$!: Observable<Review[]>;

  isSaved: boolean = false;
  isTogglingS: boolean = false;
  isLoggedIn: boolean = false;
  reportOpen: boolean = false;

  @ViewChild('thumbnailScroll') thumbnailScrollContainer!: ElementRef;

  private readonly BASE_URL = 'https://izdajemiznajmljujem.com';
  private readonly DEFAULT_TITLE = 'Izdajem Iznajmljujem';

  constructor(
    private adService: AdService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private reviewService: ReviewService,
    private userService: UserService,
    private authService: AuthService,
    private titleService: Title,
    private metaService: Meta,
  ) {}

  ngOnInit() {
    this.isLoggedIn = !!this.authService.currentUserValue;

    this.ad$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));

        if (this.isLoggedIn) {
          const viewKey = `viewed_ad_${id}`;
          if (!sessionStorage.getItem(viewKey)) {
            sessionStorage.setItem(viewKey, '1');
            this.adService.trackView(id).subscribe({ error: () => {} });
          }
        }

        return this.adService.getAdById(id);
      }),
      tap(ad => {
        this.currentAd = ad;
        this.isSaved = ad.saved ?? false;

        if (ad.images && ad.images.length > 0) {
          this.selectedImageUrl = ad.images[0];
        } else {
          this.selectedImageUrl = 'assets/images/placeholder.png';
        }

        this.updateMetaTags(ad);
        if (ad.owner && ad.owner.id) {
          this.latestReviews$ = this.reviewService.getLatestReviewsForUser(ad.owner.id).pipe(
            map(page => page.content)
          );
        }
        const currentUser = this.authService.currentUserValue;
        if (currentUser && ad.owner.id === currentUser.id) {
          this.isMyAd = true;
        }
        this.blockedIntervals = (ad.blockedIntervals || []).map(interval => ({
          start: new Date(interval.from),
          end: new Date(interval.to),
        }));
      })
    );
  }

  openReport() {
    this.reportOpen = true;
  }

  selectImage(imageUrl: string) {
    this.selectedImageUrl = imageUrl;
  }

  handleImageError(event: Event) {
    (event.target as HTMLImageElement).src = 'assets/images/placeholder.png';
  }

  openOverlay() {
    if (!this.currentAd || !this.currentAd.images || this.currentAd.images.length === 0) {
      return;
    }
    this.currentOverlayIndex = this.currentAd.images.indexOf(this.selectedImageUrl!);
    if (this.currentOverlayIndex === -1) this.currentOverlayIndex = 0;
    this.isOverlayOpen = true;
    document.body.style.overflow = 'hidden';
  }

  nextImage(event: Event) {
    event.stopPropagation();
    if (this.currentAd && this.currentAd.images) {
      this.currentOverlayIndex = (this.currentOverlayIndex + 1) % this.currentAd.images.length;
    }
  }

  previousImage(event: Event) {
    event.stopPropagation();
    if (this.currentAd && this.currentAd.images) {
      const length = this.currentAd.images.length;
      this.currentOverlayIndex = (this.currentOverlayIndex - 1 + length) % length;
    }
  }

  closeOverlay() {
    this.isOverlayOpen = false;
    document.body.style.overflow = '';
  }

  ngOnDestroy(): void {
    if (this.isOverlayOpen) {
      document.body.style.overflow = '';
    }
    this.resetMetaTags();
  }

  private updateMetaTags(ad: Ad): void {
    const title = `${ad.title} — Izdajem Iznajmljujem`;
    const desc = ad.description
      ? ad.description.substring(0, 155).replace(/\s+/g, ' ').trim() + '...'
      : `Iznajmi ${ad.title} u ${ad.location?.city ?? 'Srbiji'}. Pogledaj oglas na Izdajem Iznajmljujem.`;
    const image = (ad.images && ad.images.length > 0)
      ? ad.images[0]
      : `${this.BASE_URL}/assets/images/placeholder.png`;
    const url = `${this.BASE_URL}/ads/${ad.id}`;

    this.titleService.setTitle(title);
    this.metaService.updateTag({ name: 'description', content: desc });
    this.metaService.updateTag({ property: 'og:title', content: title });
    this.metaService.updateTag({ property: 'og:description', content: desc });
    this.metaService.updateTag({ property: 'og:image', content: image });
    this.metaService.updateTag({ property: 'og:url', content: url });
    this.metaService.updateTag({ property: 'og:type', content: 'product' });
    this.metaService.updateTag({ name: 'twitter:card', content: 'summary_large_image' });
    this.metaService.updateTag({ name: 'twitter:title', content: title });
    this.metaService.updateTag({ name: 'twitter:description', content: desc });
    this.metaService.updateTag({ name: 'twitter:image', content: image });
  }

  private resetMetaTags(): void {
    this.titleService.setTitle(this.DEFAULT_TITLE);
    this.metaService.updateTag({ name: 'description', content: 'Iznajmi sve što ti treba — alati, tehnika, oprema. Besplatno objavi oglas.' });
    this.metaService.updateTag({ property: 'og:title', content: this.DEFAULT_TITLE });
    this.metaService.updateTag({ property: 'og:description', content: 'Iznajmi sve što ti treba — alati, tehnika, oprema. Besplatno objavi oglas.' });
    this.metaService.updateTag({ property: 'og:image', content: `${this.BASE_URL}/assets/images/placeholder.png` });
    this.metaService.updateTag({ property: 'og:url', content: this.BASE_URL });
    this.metaService.updateTag({ property: 'og:type', content: 'website' });
  }

  scrollThumbnails(amount: number) {
    if (this.thumbnailScrollContainer) {
      this.thumbnailScrollContainer.nativeElement.scrollBy({
        top: amount,
        behavior: 'smooth'
      });
    }
  }

  revealPhoneNumber(ownerId: number): void {
    if (this.isLoadingPhone) return;
    this.isLoadingPhone = true;
    this.userService.getPhoneNumber(ownerId).subscribe({
      next: (res) => {
        this.realPhoneNumber = res.phone;
        this.isLoadingPhone = false;
      },
      error: () => {
        this.isLoadingPhone = false;
        this.toastService.showError('Greška pri učitavanju broja telefona.');
      }
    });
  }

  startChat(): void {
    if (!this.currentAd) return;

    if (!this.authService.currentUserValue) {
      this.toastService.showError('Morate biti ulogovani da biste poslali poruku.');
      this.router.navigate(['/login']);
      return;
    }

    const currentUser = this.authService.currentUserValue;
    if (currentUser && currentUser.id === this.currentAd.owner.id) {
      this.toastService.showError('Ne možete poslati poruku samom sebi.');
      return;
    }

    this.router.navigate(['/messages'], {
      queryParams: {
        newChatAdId: this.currentAd.id,
        receiverId: this.currentAd.owner.id,
        adTitle: this.currentAd.title,
        receiverName: this.currentAd.owner.displayName
      }
    });
  }

  toggleSave(): void {
    if (this.isTogglingS || !this.currentAd) return;
    this.isTogglingS = true;

    const wasSaved = this.isSaved;
    this.isSaved = !wasSaved;

    const request$ = wasSaved
      ? this.adService.unsaveAd(this.currentAd.id)
      : this.adService.saveAd(this.currentAd.id);

    request$.subscribe({
      next: () => {
        this.isTogglingS = false;
      },
      error: () => {
        this.isSaved = wasSaved;
        this.isTogglingS = false;
      }
    });
  }
}
