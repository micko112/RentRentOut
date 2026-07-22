import {Component, ElementRef, HostListener, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewChild} from '@angular/core';
import {Ad} from '../../../../shared/models/ad.model';
import {CommonModule, isPlatformBrowser} from '@angular/common';
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
import {SeoService} from '../../../../core/services/seo.service';
import {FormsModule} from '@angular/forms';
import {PromotionService} from '../../services/promotion.service';
import {PromotionModalComponent} from '../../components/promotion-modal/promotion-modal.component';

@Component({
  selector: 'app-ad-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, InitialsPipe, ReviewCardComponent, RentalCalendarComponent, ReportModalComponent, PromotionModalComponent],
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

  showPromoModal = false;
  renewingInProgress = false;
  showDeleteModal = false;
  deleteReason = '';
  readonly deleteReasons = [
    'Više ne izdajem ovaj predmet',
    'Pronašao/la sam zakupca na drugom mestu',
    'Oglas sadrži grešku',
    'Ostalo'
  ];

  @ViewChild('thumbnailScroll') thumbnailScrollContainer!: ElementRef;

  constructor(
    private adService: AdService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private reviewService: ReviewService,
    private userService: UserService,
    private authService: AuthService,
    private seoService: SeoService,
    private promotionService: PromotionService,
    @Inject(PLATFORM_ID) private platformId: object,
  ) {}

  ngOnInit() {
    this.isLoggedIn = !!this.authService.currentUserValue;

    this.ad$ = this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));

        if (this.isLoggedIn && isPlatformBrowser(this.platformId)) {
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

        this.seoService.setAdPage(ad);
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

  get waShareUrl(): string {
    if (!isPlatformBrowser(this.platformId)) return '#';
    const text = `Pogledaj ovaj oglas: ${window.location.href}`;
    return `https://wa.me/?text=${encodeURIComponent(text)}`;
  }

  get fbShareUrl(): string {
    if (!isPlatformBrowser(this.platformId)) return '#';
    return `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(window.location.href)}`;
  }

  openReport() {
    this.reportOpen = true;
  }

  selectImage(imageUrl: string) {
    this.selectedImageUrl = imageUrl;
  }

  prevMainImage(event?: Event) {
    if (event) event.stopPropagation();
    if (!this.currentAd?.images?.length) return;
    const len = this.currentAd.images.length;
    const idx = this.currentAd.images.indexOf(this.selectedImageUrl!);
    const next = idx === -1 ? 0 : (idx - 1 + len) % len;
    this.selectedImageUrl = this.currentAd.images[next];
  }

  nextMainImage(event?: Event) {
    if (event) event.stopPropagation();
    if (!this.currentAd?.images?.length) return;
    const len = this.currentAd.images.length;
    const idx = this.currentAd.images.indexOf(this.selectedImageUrl!);
    const next = idx === -1 ? 0 : (idx + 1) % len;
    this.selectedImageUrl = this.currentAd.images[next];
  }

  private touchStartX = 0;
  private touchStartY = 0;
  private touchMoved = false;

  onTouchStart(event: TouchEvent) {
    this.touchStartX = event.touches[0].clientX;
    this.touchStartY = event.touches[0].clientY;
    this.touchMoved = false;
  }

  onTouchMove(event: TouchEvent) {
    const dx = Math.abs(event.touches[0].clientX - this.touchStartX);
    const dy = Math.abs(event.touches[0].clientY - this.touchStartY);
    if (dx > 10 && dx > dy) this.touchMoved = true;
  }

  onTouchEnd(event: TouchEvent) {
    if (!this.touchMoved) return;
    const dx = event.changedTouches[0].clientX - this.touchStartX;
    const dy = Math.abs(event.changedTouches[0].clientY - this.touchStartY);
    if (Math.abs(dx) > 40 && Math.abs(dx) > dy) {
      event.stopPropagation();
      event.preventDefault();
      if (dx < 0) this.nextMainImage();
      else this.prevMainImage();
    }
  }

  handleImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    const src = img.src;
    if (src && /\.(heic|heif)(\?.*)?$/i.test(src)) {
      img.onerror = () => { img.src = 'assets/images/placeholder.png'; img.onerror = null; };
      img.src = src.replace(/\.(heic|heif)(\?.*)?$/i, '.jpg');
      return;
    }
    img.src = 'assets/images/placeholder.png';
    img.onerror = null;
  }

  openOverlay() {
    if (this.touchMoved) { this.touchMoved = false; return; }
    if (!this.currentAd || !this.currentAd.images || this.currentAd.images.length === 0) {
      return;
    }
    this.currentOverlayIndex = this.currentAd.images.indexOf(this.selectedImageUrl!);
    if (this.currentOverlayIndex === -1) this.currentOverlayIndex = 0;
    this.isOverlayOpen = true;
    if (isPlatformBrowser(this.platformId)) document.body.style.overflow = 'hidden';
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
    if (isPlatformBrowser(this.platformId)) document.body.style.overflow = '';
  }

  @HostListener('window:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (!this.isOverlayOpen) return;
    if (event.key === 'ArrowRight')      this.nextImage(event);
    else if (event.key === 'ArrowLeft')  this.previousImage(event);
    else if (event.key === 'Escape')     this.closeOverlay();
  }

  // Swipe (touch) navigacija u overlay-u
  private overlayTouchStartX: number | null = null;
  onOverlayTouchStart(e: TouchEvent): void {
    if (e.touches.length === 1) this.overlayTouchStartX = e.touches[0].clientX;
  }
  onOverlayTouchEnd(e: TouchEvent): void {
    if (this.overlayTouchStartX === null) return;
    const endX = e.changedTouches[0].clientX;
    const dx = endX - this.overlayTouchStartX;
    this.overlayTouchStartX = null;
    if (Math.abs(dx) < 40) return;
    if (dx < 0) this.nextImage(e);
    else        this.previousImage(e);
  }

  ngOnDestroy(): void {
    if (this.isOverlayOpen) {
      if (isPlatformBrowser(this.platformId)) document.body.style.overflow = '';
    }
    this.seoService.reset();
  }

  // ── Real estate label helpers ────────────────────────────────────────────
  private static readonly RE_LABELS: Record<string, string> = {
    AGENCIJA: 'Agencija', VLASNIK: 'Vlasnik', INVESTITOR: 'Investitor',
    NOVOGRADNJA: 'Novogradnja', STARA_GRADNJA: 'Stara gradnja',
    IZVORNO_STANJE: 'Izvorno stanje', U_IZGRADNJI: 'U izgradnji', RENOVIRANO: 'Renovirano',
    POTREBNO_RENOVIRANJE: 'Potrebno renoviranje', LUKSUZNO: 'Luksuzno',
    NAMESTENO: 'Namešteno', POLUNAMESTENO: 'Polunaměšteno', PRAZNO: 'Prazno',
    PODRUM: 'Podrum', SUTEREN: 'Suteren', NISKO_PRIZEMLJE: 'Nisko prizemlje',
    PRIZEMLJE: 'Prizemlje', VISOKO_PRIZEMLJE: 'Visoko prizemlje', POTKROVLJE: 'Potkrovlje',
    CENTRALNO: 'Centralno', KLIMA: 'Klima', ETAZNO: 'Etažno',
    TOPLOTNA_PUMPA: 'Toplotna pumpa', STRUJA: 'Struja', GAS: 'Gas',
    MERMERNI_RADIJATORI: 'Mermerni radijatori', NORVESKI_RADIJATORI: 'Norveški radijatori',
    CVRSTO_GORIVO: 'Čvrsto gorivo', TA_PEC: 'TA peć',
    // Broj soba — stanovi
    '0.5': 'Garsonjera', '1.0': 'Jednosoban', '1.5': 'Jednoiposoban',
    '2.0': 'Dvosoban', '2.5': 'Dvoiposoban', '3.0': 'Trosoban',
    '3.5': 'Troiposoban', '4.0': 'Četvorosoban', '4.5': 'Četvoroiposoban', '5+': 'Petosoban i veći',
    // Broj soba — kuće
    '1': '1 soba', '2': '2 sobe', '3': '3 sobe', '4': '4 sobe', '5_PLUS': '5+ soba',
    // Spratnost — kuće
    PRIZEMNA: 'Prizemna', '3+': '3+ sprata',
    // Jedinice površine zemljišta
    ar: 'ar', m2: 'm²', jutro: 'jutro', hektar: 'ha',
    // Dodatne karakteristike
    ODMAH_USELJIVO: 'Odmah useljivo', VIDEO_NADZOR: 'Video nadzor',
    POMOCNI_OBJEKTI: 'Pomoćni objekti', VODA: 'Voda', INTERNET: 'Internet',
    INTERFON: 'Interfon', TERASA: 'Terasa', KABLOVA_TV: 'Kablovska TV',
    KLIMA_UREDJAJ: 'Klima', ENERGETSKI_PASOS: 'Energetski pasoš',
    PRISTUP_INVALIDIMA: 'Prilaz za invalide', PARKING: 'Parking',
    GARAZA: 'Garaža', BAZEN: 'Bazen', KANALIZACIJA: 'Kanalizacija',
    STRUJA_PRIKLJUCAK: 'Struja', ASFALTIRAN_PRILAZ: 'Asfaltiran prilaz',
    BASTA: 'Bašta', IZLOG: 'Izlog',
    PET_FRIENDLY: 'Pet friendly', DEPOZIT: 'Depozit',
    OSTALO: 'Ostalo',
    // Automobili — karoserija
    LIMUZINA: 'Limuzina', HECBEK: 'Hečbek', KAR: 'Karavan', SUV: 'SUV / Džip',
    MONOVOLUMEN: 'Monovolumen', KABRIOLET: 'Kabriolet', KUPE: 'Kupe', PIKAP: 'Pikap', KOMBI: 'Kombi',
    // Automobili — gorivo
    BENZIN: 'Benzin', DIZEL: 'Dizel', TNG: 'TNG', METAN: 'Metan (CNG)',
    HIBRID: 'Hibrid', ELEKTRICNI: 'Električni', VODIK: 'Vodik',
    // Automobili — menjač
    MANUELNI: 'Manuelni', AUTOMATSKI: 'Automatski', POLUAUTOMATSKI: 'Poluautomatski',
    // Automobili — pogon
    PREDNJI: 'Prednji pogon', ZADNJI: 'Zadnji pogon', '4X4': '4x4 / AWD',
    // Automobili — vrata
    '2/3': '2/3 vrata', '4/5': '4/5 vrata',
    // Automobili — volan
    LEVI: 'Levi volan', DESNI: 'Desni volan',
    // Automobili — poreklo
    DOMACE: 'Domaće', UVOZ: 'Uvoz', IZ_EU: 'Iz EU',
    // Automobili — vlasništvo
    PRIVATNO: 'Privatno', FIRMA: 'Firma', STRANO: 'Strano',
    // Automobili — oštećenje
    NEOSTECAN: 'Neoštećen', OSTECEN_VOZNO: 'Oštećen — vozno',
    OSTECEN_NEVOZNO: 'Oštećen — nevozno', RASHODOVANO: 'Rashodovano',
    // Automobili — boja
    CRNA: 'Crna', BELA: 'Bela', SIVA: 'Siva', CRVENA: 'Crvena', PLAVA: 'Plava',
    ZELENA: 'Zelena', ZUTA: 'Žuta', NARANDZASTA: 'Narandžasta', SMEDA: 'Smeđa',
    ZLATNA: 'Zlatna', SREBRNA: 'Srebrna',
    // Automobili — emisiona klasa
    EURO1: 'Euro 1', EURO2: 'Euro 2', EURO3: 'Euro 3',
    EURO4: 'Euro 4', EURO5: 'Euro 5', EURO6: 'Euro 6',
    // Automobili — enterijer
    TKANINA: 'Tkanina', KOZA: 'Koža', VESTACKA_KOZA: 'Veštačka koža', KOMBINOVANO: 'Kombinovano',
    BEZ: 'Bež',
    // Automobili — oprema
    AUTO_KLIMA: 'Automatska klima',
    PANORAMSKI_KROV: 'Panoramski krov', GPS_NAVIGACIJA: 'GPS navigacija',
    PARKING_SENZORI_ZAD: 'Parking senzori (zadnji)', PARKING_SENZORI_PRED: 'Parking senzori (prednji)',
    KAMERA_ZADNJA: 'Kamera za vožnju unazad', TEMPOMAT: 'Tempomat',
    BLUETOOTH: 'Bluetooth / handsfree', KOZNA_SEDISTA: 'Kožna sedišta',
    GREJANJE_SEDISTA: 'Grejanje sedišta', ELEKTRICNI_PROZORI: 'Električni prozori',
    ALU_FELNE: 'Alu felne', LED_SVETLA: 'LED svetla', XENON_SVETLA: 'Xenon svetla',
    MULTIFUNKCIJSKI_VOLAN: 'Multifunkcijski volan', START_STOP: 'Start-stop sistem',
    HEAD_UP_DISPLAY: 'Head-up display', PRIKLJUCAK_PRIKOLICA: 'Priključak za prikolicu',
    KROVNI_NOSAC: 'Krovni nosač',
  };

  reLabel(value: string | undefined | null): string {
    if (!value) return '';
    return AdDetailsComponent.RE_LABELS[value] ?? value;
  }

  reFloorLabel(value: string | undefined | null): string {
    if (!value) return '';
    const mapped = AdDetailsComponent.RE_LABELS[value];
    if (mapped) return mapped;
    const n = Number(value);
    if (!isNaN(n)) return `${n}. sprat`;
    return value;
  }

  reFloorsLabel(value: string | undefined | null): string {
    if (!value) return '';
    const mapped = AdDetailsComponent.RE_LABELS[value];
    if (mapped) return mapped;
    const n = Number(value);
    if (isNaN(n)) return value;
    return n === 1 ? '1 sprat' : n < 5 ? `${n} sprata` : `${n} spratova`;
  }

  landAreaUnitLabel(unit: string | undefined | null): string {
    if (!unit) return '';
    return AdDetailsComponent.RE_LABELS[unit] ?? unit;
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

  renewAd(): void {
    if (this.renewingInProgress || !this.currentAd) return;
    this.renewingInProgress = true;
    this.promotionService.renewAd(this.currentAd.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Oglas je obnovljen na 30 dana.');
        this.renewingInProgress = false;
      },
      error: () => {
        this.toastService.showError('Greška pri obnovi oglasa.');
        this.renewingInProgress = false;
      }
    });
  }

  openPromoModal(): void  { this.showPromoModal = true;  }
  closePromoModal(): void { this.showPromoModal = false; }

  openDeleteModal(): void  { this.showDeleteModal = true;  }
  closeDeleteModal(): void { this.showDeleteModal = false; this.deleteReason = ''; }

  confirmDelete(): void {
    if (!this.currentAd || !this.deleteReason) return;
    const id = this.currentAd.id;
    this.closeDeleteModal();
    this.adService.deleteAd(id).subscribe({
      next: () => {
        this.toastService.showSuccess('Uspešno ste obrisali oglas!');
        this.router.navigate(['/user/me/ads']);
      },
      error: () => this.toastService.showError('Greška pri brisanju oglasa.')
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
