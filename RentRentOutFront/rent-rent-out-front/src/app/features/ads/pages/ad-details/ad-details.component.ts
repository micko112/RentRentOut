import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
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

@Component({
  selector: 'app-ad-details',
  standalone: true,
  imports: [CommonModule, RouterLink, InitialsPipe, ReviewCardComponent, RentalCalendarComponent],
  templateUrl: './ad-details.component.html',
  styleUrl: './ad-details.component.css'
})
export class AdDetailsComponent implements OnInit {
  ad$!: Observable<Ad>;
  selectedImageUrl: string | null = null;

  isOverlayOpen: boolean = false;
  currentOverlayIndex: number = 0;

  isMyAd: boolean = false;
  currentAd!: Ad;
  blockedIntervals: { start: Date, end: Date }[] = [];
  showDeleteConfirm: boolean = false;

  realPhoneNumber: string | null = null;
  isLoadingPhone: boolean = false;

  latestReviews$!: Observable<Review[]>;

  @ViewChild('thumbnailScroll') thumbnailScrollContainer!: ElementRef;

  constructor(
    private adService: AdService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private reviewService: ReviewService,
    private userService: UserService,
    private authService: AuthService,
  ) {}

  ngOnInit() {
    this.ad$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        return this.adService.getAdById(id);
      }),
      tap(ad => {
        this.currentAd = ad;
        if (ad.images && ad.images.length > 0) {
          this.selectedImageUrl = ad.images[0];
        } else {
          this.selectedImageUrl = 'assets/images/placeholder.png';
        }
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

  selectImage(imageUrl: string) {
    this.selectedImageUrl = imageUrl;
  }

  handleImageError(event: any) {
    event.target.src = 'assets/images/placeholder.png';
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
    document.body.style.overflow = 'auto';
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
        this.realPhoneNumber = 'Greška pri učitavanju';
        this.isLoadingPhone = false;
      }
    });
  }

  deleteAd(): void {
    if (!this.currentAd) return;
    this.adService.deleteAd(this.currentAd.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.toastService.showSuccess('Oglas je uspešno obrisan.');
        this.router.navigate(['/ads']);
      },
      error: (err) => {
        this.showDeleteConfirm = false;
        this.toastService.showError(err.error?.message || 'Greška pri brisanju oglasa.');
      }
    });
  }

  parkAd(): void {
    if (!this.currentAd) return;
    this.adService.updateAdStatus(this.currentAd.id, 'PAUSED').subscribe({
      next: (updated) => {
        this.currentAd = { ...this.currentAd, adStatus: updated.adStatus };
        this.toastService.showSuccess('Oglas je parkiran i više nije vidljiv u pretrazi.');
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Greška pri parkiranju.')
    });
  }

  unparkAd(): void {
    if (!this.currentAd) return;
    this.adService.updateAdStatus(this.currentAd.id, 'ACTIVE').subscribe({
      next: (updated) => {
        this.currentAd = { ...this.currentAd, adStatus: updated.adStatus };
        this.toastService.showSuccess('Oglas je ponovo aktivan.');
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Greška pri obnavljanju.')
    });
  }

  getAdStatusLabel(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'Aktivan',
      PAUSED: 'Parkiran',
      RENTED: 'Iznajmljeno',
      ARCHIVED: 'Arhiviran',
      DELETED: 'Obrisan',
    };
    return map[status] ?? status;
  }

  startChat(): void {
    if (!this.currentAd) return;

    const token = localStorage.getItem('authToken');
    if (!token) {
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
}
