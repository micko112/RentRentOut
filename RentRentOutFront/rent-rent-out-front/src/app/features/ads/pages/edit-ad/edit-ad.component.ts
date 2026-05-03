import {Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AdService} from '../../services/ad.service';
import {CategoryService} from '../../services/category.service';
import {LocationService} from '../../services/location.service';
import {Category} from '../../../../shared/models/category.model';
import {PriceInterval} from '../../../../shared/models/price-interval.enum';
import {ToastService} from '../../../../shared/services/toast.service';
import {Ad} from '../../../../shared/models/ad.model';
import {UpdateAdRequest} from '../../../../shared/models/update-ad-request';
import {Location} from '../../../../shared/models/location.model';

@Component({
  selector: 'app-edit-ad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-ad.component.html',
  styleUrl: './edit-ad.component.css'
})
export class EditAdComponent implements OnInit, OnDestroy {

  // ── Step state ──────────────────────────────────────────────────────────
  currentStep = 1;
  readonly totalSteps = 2;

  // ── Form ────────────────────────────────────────────────────────────────
  form!: FormGroup;
  readonly MAX_TITLE = 100;
  readonly MAX_DESC = 2000;

  // ── Categories ──────────────────────────────────────────────────────────
  categories: Category[] = [];

  // ── Price intervals ─────────────────────────────────────────────────────
  readonly priceIntervalOptions = [
    { value: PriceInterval.PER_HOUR,  label: 'Po satu',   unit: '/h'   },
    { value: PriceInterval.PER_DAY,   label: 'Po danu',   unit: '/dan' },
    { value: PriceInterval.PER_MONTH, label: 'Po mesecu', unit: '/mes' },
  ];

  // ── Locations ───────────────────────────────────────────────────────────
  locations: Location[] = [];
  filteredLocations: Location[] = [];
  locationSearch = '';
  showLocationDropdown = false;

  @ViewChild('descEditor') descEditorRef?: ElementRef<HTMLDivElement>;
  @ViewChild('locTriggerRef') locTriggerRef!: ElementRef;
  locDropdownStyle: {[key: string]: string} = {};

  // ── Images ──────────────────────────────────────────────────────────────
  selectedFiles: File[] = [];
  previewUrl: string[] = [];
  existingImages: string[] = [];
  isDragging = false;
  readonly MAX_IMAGES = 10;

  // ── Real estate ──────────────────────────────────────────────────────────
  readonly RE_ROOT_ID = 900;

  readonly reAdvertiserOptions = ['AGENCIJA', 'VLASNIK', 'INVESTITOR'];
  readonly reAdvertiserLabels: Record<string, string> = {
    AGENCIJA: 'Agencija', VLASNIK: 'Vlasnik', INVESTITOR: 'Investitor'
  };
  readonly reRoomOptions = [
    { value: '0.5', label: 'Garsonjera' }, { value: '1.0', label: 'Jednosoban' },
    { value: '1.5', label: 'Jednoiposoban' }, { value: '2.0', label: 'Dvosoban' },
    { value: '2.5', label: 'Dvoiposoban' }, { value: '3.0', label: 'Trosoban' },
    { value: '3.5', label: 'Troiposoban' }, { value: '4.0', label: 'Četvorosoban' },
    { value: '4.5', label: 'Četvoroiposoban' }, { value: '5+', label: 'Petosoban i veći' },
  ];
  readonly reConstructionOptions = [
    { value: 'NOVOGRADNJA', label: 'Novogradnja' }, { value: 'STARA_GRADNJA', label: 'Stara gradnja' },
  ];
  readonly reConditionOptions = [
    { value: 'IZVORNO_STANJE', label: 'Izvorno stanje' }, { value: 'U_IZGRADNJI', label: 'U izgradnji' },
    { value: 'RENOVIRANO', label: 'Renovirano' }, { value: 'POTREBNO_RENOVIRANJE', label: 'Potrebno renoviranje' },
    { value: 'LUKSUZNO', label: 'Luksuzno' },
  ];
  readonly reTotalFloorOptions: { value: string; label: string }[] = (() => {
    const opts = [];
    for (let i = 1; i <= 30; i++) {
      opts.push({ value: String(i), label: i === 1 ? '1 sprat' : i < 5 ? `${i} sprata` : `${i} spratova` });
    }
    opts.push({ value: '30+', label: '30+ spratova' });
    return opts;
  })();
  readonly reFloorOptions: { value: string; label: string }[] = (() => {
    const s = [
      { value: 'PODRUM', label: 'Podrum' }, { value: 'SUTEREN', label: 'Suteren' },
      { value: 'NISKO_PRIZEMLJE', label: 'Nisko prizemlje' }, { value: 'PRIZEMLJE', label: 'Prizemlje' },
      { value: 'VISOKO_PRIZEMLJE', label: 'Visoko prizemlje' }, { value: 'POTKROVLJE', label: 'Potkrovlje' },
    ];
    for (let i = 1; i <= 30; i++) s.push({ value: String(i), label: `${i}. sprat` });
    s.push({ value: '30+', label: '30+. sprat' });
    return s;
  })();
  readonly reFurnishedOptions = [
    { value: 'NAMESTENO', label: 'Namešteno' }, { value: 'POLUNAMESTENO', label: 'Polunaměšteno' },
    { value: 'PRAZNO', label: 'Prazno' },
  ];
  readonly reHeatingOptions = [
    { value: 'CENTRALNO', label: 'Centralno' }, { value: 'KLIMA', label: 'Klima' },
    { value: 'ETAZNO', label: 'Etažno' }, { value: 'TOPLOTNA_PUMPA', label: 'Toplotna pumpa' },
    { value: 'STRUJA', label: 'Struja' }, { value: 'GAS', label: 'Gas' },
    { value: 'MERMERNI_RADIJATORI', label: 'Mermerni radijatori' },
    { value: 'NORVESKI_RADIJATORI', label: 'Norveški radijatori' },
    { value: 'CVRSTO_GORIVO', label: 'Čvrsto gorivo' }, { value: 'TA_PEC', label: 'TA peć' },
  ];

  readonly reHouseRoomOptions = [
    { value: '1',      label: '1 soba' },
    { value: '2',      label: '2 sobe' },
    { value: '3',      label: '3 sobe' },
    { value: '4',      label: '4 sobe' },
    { value: '5_PLUS', label: '5+ soba' },
  ];

  readonly reHouseTotalFloorOptions = [
    { value: 'PRIZEMNA', label: 'Prizemna' },
    { value: '1',        label: '1 sprat' },
    { value: '2',        label: '2 sprata' },
    { value: '3+',       label: '3+ sprata' },
  ];

  readonly reLandAreaUnitOptions = [
    { value: 'ar',     label: 'ar' },
    { value: 'm2',     label: 'm²' },
    { value: 'jutro',  label: 'jutro' },
    { value: 'hektar', label: 'ha' },
  ];

  // ── Vehicle / Car ────────────────────────────────────────────────────────
  readonly VEHICLE_ROOT_ID = 800;

  readonly carBodyTypeOptions = [
    { value: 'LIMUZINA',    label: 'Limuzina' },
    { value: 'HECBEK',      label: 'Hečbek' },
    { value: 'KAR',         label: 'Karavan' },
    { value: 'SUV',         label: 'SUV / Džip' },
    { value: 'MONOVOLUMEN', label: 'Monovolumen' },
    { value: 'KABRIOLET',   label: 'Kabriolet' },
    { value: 'KUPE',        label: 'Kupe' },
    { value: 'PIKAP',       label: 'Pikap' },
    { value: 'KOMBI',       label: 'Kombi' },
    { value: 'OSTALO',      label: 'Ostalo' },
  ];
  readonly carFuelTypeOptions = [
    { value: 'BENZIN', label: 'Benzin' }, { value: 'DIZEL', label: 'Dizel' },
    { value: 'TNG', label: 'TNG' }, { value: 'METAN', label: 'Metan (CNG)' },
    { value: 'HIBRID', label: 'Hibrid' }, { value: 'ELEKTRICNI', label: 'Električni' },
    { value: 'VODIK', label: 'Vodik' },
  ];
  readonly carTransmissionOptions = [
    { value: 'MANUELNI', label: 'Manuelni' }, { value: 'AUTOMATSKI', label: 'Automatski' },
    { value: 'POLUAUTOMATSKI', label: 'Poluautomatski' },
  ];
  readonly carDriveOptions = [
    { value: 'PREDNJI', label: 'Prednji pogon' }, { value: 'ZADNJI', label: 'Zadnji pogon' },
    { value: '4X4', label: '4x4 / AWD' },
  ];
  readonly carDoorsOptions = [
    { value: '2/3', label: '2/3 vrata' }, { value: '4/5', label: '4/5 vrata' },
  ];
  readonly carSteeringWheelOptions = [
    { value: 'LEVI', label: 'Levi volan' }, { value: 'DESNI', label: 'Desni volan' },
  ];
  readonly carOriginOptions = [
    { value: 'DOMACE', label: 'Domaće' }, { value: 'UVOZ', label: 'Uvoz' }, { value: 'IZ_EU', label: 'Iz EU' },
  ];
  readonly carOwnershipOptions = [
    { value: 'PRIVATNO', label: 'Privatno' }, { value: 'FIRMA', label: 'Firma' }, { value: 'STRANO', label: 'Strano' },
  ];
  readonly carDamageOptions = [
    { value: 'NEOSTECAN',       label: 'Neoštećen' },
    { value: 'OSTECEN_VOZNO',   label: 'Oštećen — vozno' },
    { value: 'OSTECEN_NEVOZNO', label: 'Oštećen — nevozno' },
    { value: 'RASHODOVANO',     label: 'Rashodovano' },
  ];
  readonly carColorOptions = [
    { value: 'CRNA', label: 'Crna' }, { value: 'BELA', label: 'Bela' }, { value: 'SIVA', label: 'Siva' },
    { value: 'CRVENA', label: 'Crvena' }, { value: 'PLAVA', label: 'Plava' }, { value: 'ZELENA', label: 'Zelena' },
    { value: 'ZUTA', label: 'Žuta' }, { value: 'NARANDZASTA', label: 'Narandžasta' },
    { value: 'SMEDA', label: 'Smeđa' }, { value: 'BORDO', label: 'Bordo' },
    { value: 'ZLATNA', label: 'Zlatna' }, { value: 'SREBRNA', label: 'Srebrna' }, { value: 'OSTALO', label: 'Ostalo' },
  ];
  readonly carEmissionOptions = [
    { value: 'EURO1', label: 'Euro 1' }, { value: 'EURO2', label: 'Euro 2' },
    { value: 'EURO3', label: 'Euro 3' }, { value: 'EURO4', label: 'Euro 4' },
    { value: 'EURO5', label: 'Euro 5' }, { value: 'EURO6', label: 'Euro 6' },
  ];
  readonly carInteriorMaterialOptions = [
    { value: 'TKANINA', label: 'Tkanina' }, { value: 'KOZA', label: 'Koža' },
    { value: 'VESTACKA_KOZA', label: 'Veštačka koža' }, { value: 'KOMBINOVANO', label: 'Kombinovano' },
  ];
  readonly carInteriorColorOptions = [
    { value: 'CRNA', label: 'Crna' }, { value: 'SIVA', label: 'Siva' }, { value: 'BELA', label: 'Bela' },
    { value: 'SMEDA', label: 'Smeđa' }, { value: 'BEZ', label: 'Bež' }, { value: 'BORDO', label: 'Bordo' },
  ];
  readonly carLabelOptions = [
    { value: 'A', label: 'A' }, { value: 'B', label: 'B' }, { value: 'C', label: 'C' },
    { value: 'D', label: 'D' }, { value: 'E', label: 'E' }, { value: 'F', label: 'F' }, { value: 'G', label: 'G' },
  ];
  readonly carEquipmentOptions = [
    { value: 'KLIMA',                label: 'Klima' },
    { value: 'AUTO_KLIMA',           label: 'Automatska klima' },
    { value: 'PANORAMSKI_KROV',      label: 'Panoramski krov' },
    { value: 'GPS_NAVIGACIJA',       label: 'GPS navigacija' },
    { value: 'PARKING_SENZORI_ZAD',  label: 'Parking senzori (zadnji)' },
    { value: 'PARKING_SENZORI_PRED', label: 'Parking senzori (prednji)' },
    { value: 'KAMERA_ZADNJA',        label: 'Kamera za vožnju unazad' },
    { value: 'TEMPOMAT',             label: 'Tempomat' },
    { value: 'BLUETOOTH',            label: 'Bluetooth / handsfree' },
    { value: 'KOZNA_SEDISTA',        label: 'Kožna sedišta' },
    { value: 'GREJANJE_SEDISTA',     label: 'Grejanje sedišta' },
    { value: 'ELEKTRICNI_PROZORI',   label: 'Električni prozori' },
    { value: 'ALU_FELNE',            label: 'Alu felne' },
    { value: 'LED_SVETLA',           label: 'LED svetla' },
    { value: 'XENON_SVETLA',         label: 'Xenon svetla' },
    { value: 'MULTIFUNKCIJSKI_VOLAN', label: 'Multifunkcijski volan' },
    { value: 'START_STOP',           label: 'Start-stop sistem' },
    { value: 'HEAD_UP_DISPLAY',      label: 'Head-up display' },
    { value: 'PRIKLJUCAK_PRIKOLICA', label: 'Priključak za prikolicu' },
    { value: 'KROVNI_NOSAC',         label: 'Krovni nosač' },
  ];

  readonly reCommercialFeatures = [
    { value: 'INTERNET',           label: 'Internet' },
    { value: 'TERASA',             label: 'Terasa' },
    { value: 'KLIMA_UREDJAJ',      label: 'Klima' },
    { value: 'VIDEO_NADZOR',       label: 'Video nadzor' },
    { value: 'PRISTUP_INVALIDIMA', label: 'Prilaz za invalide' },
    { value: 'PARKING',            label: 'Parking' },
    { value: 'ENERGETSKI_PASOS',   label: 'Energetski pasoš' },
    { value: 'GARAZA',             label: 'Garaža' },
    { value: 'STRUJA_PRIKLJUCAK',  label: 'Struja' },
    { value: 'VODA',               label: 'Voda' },
    { value: 'ASFALTIRAN_PRILAZ',  label: 'Asfaltiran prilaz' },
    { value: 'BASTA',              label: 'Bašta' },
    { value: 'IZLOG',              label: 'Izlog' },
    { value: 'PET_FRIENDLY',       label: 'Pet friendly' },
    { value: 'DEPOZIT',            label: 'Depozit' },
  ];

  readonly reHouseFeatures = [
    { value: 'ODMAH_USELJIVO',     label: 'Odmah useljivo' },
    { value: 'PODRUM',             label: 'Podrum' },
    { value: 'VIDEO_NADZOR',       label: 'Video nadzor' },
    { value: 'POMOCNI_OBJEKTI',    label: 'Pomoćni objekti' },
    { value: 'VODA',               label: 'Voda' },
    { value: 'INTERNET',           label: 'Internet' },
    { value: 'INTERFON',           label: 'Interfon' },
    { value: 'TERASA',             label: 'Terasa' },
    { value: 'KABLOVA_TV',         label: 'Kablovska TV' },
    { value: 'KLIMA_UREDJAJ',      label: 'Klima' },
    { value: 'ENERGETSKI_PASOS',   label: 'Energetski pasoš' },
    { value: 'PRISTUP_INVALIDIMA', label: 'Prilaz za invalide' },
    { value: 'PARKING',            label: 'Parking' },
    { value: 'GARAZA',             label: 'Garaža' },
    { value: 'BAZEN',              label: 'Bazen' },
    { value: 'KANALIZACIJA',       label: 'Kanalizacija' },
    { value: 'STRUJA_PRIKLJUCAK',  label: 'Struja' },
    { value: 'ASFALTIRAN_PRILAZ',  label: 'Asfaltiran prilaz' },
    { value: 'PET_FRIENDLY',       label: 'Pet friendly' },
    { value: 'DEPOZIT',            label: 'Depozit' },
  ];

  // ── Misc ────────────────────────────────────────────────────────────────
  adId!: number;
  currentAd!: Ad;
  isLoading = true;
  isSubmitting = false;

  readonly stepConfig = [
    { label: 'Kategorija i opis' },
    { label: 'Slike, cena i lokacija' },
  ];

  constructor(private adService: AdService,
              private categoryService: CategoryService,
              private locationService: LocationService,
              private fb: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private toastService: ToastService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(this.MAX_TITLE)]],
      description: ['', [
        c => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length > 0 ? null : { required: true }; },
        c => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length >= 20 ? null : { minlength: { requiredLength: 20 } }; },
        c => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length <= this.MAX_DESC ? null : { maxlength: { requiredLength: this.MAX_DESC } }; },
      ]],
      price: [null, [Validators.required, Validators.min(1)]],
      currency: ['RSD', [Validators.required]],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      images: [[], Validators.required],
      pricePerWeek:  [null],
      pricePerMonth: [null],
      deposit:       [null, [Validators.min(0)]],
      advertiserType:       [null],
      roomCount:            [null],
      areaSize:             [null],
      constructionType:     [null],
      propertyCondition:    [null],
      totalFloors:          [null],
      floorNumber:          [null],
      furnished:            [null],
      heatingTypes:         [[]],
      propertyMunicipality: [null],
      propertyNeighborhood: [null],
      propertyStreet:       [null],
      landArea:             [null],
      landAreaUnit:         ['ar'],
      features:             [[]],
      carBrand:            [null],
      carModel:            [null],
      carYear:             [null],
      carMileage:          [null],
      carBodyType:         [null],
      carFuelType:         [null],
      carTransmission:     [null],
      carPowerKw:          [null],
      carColor:            [null],
      carDoors:            [null],
      carSeats:            [null],
      carDisplacement:     [null],
      carEmissionClass:    [null],
      carDrive:            [null],
      carSteeringWheel:    [null],
      carRegisteredUntil:  [null],
      carCountry:          [null],
      carOrigin:           [null],
      carOwnership:        [null],
      carDamage:           [null],
      carLabel:            [null],
      carInteriorMaterial: [null],
      carInteriorColor:    [null],
    });

    const paramId = this.route.snapshot.paramMap.get('id');
    this.adId = paramId ? Number(paramId) : NaN;

    if (!this.adId || Number.isNaN(this.adId)) {
      this.toastService.showError('Neispravan ID oglasa.');
      this.router.navigate(['/user/me/ads']);
      return;
    }

    this.categoryService.getAll().subscribe({
      next: (cats) => { this.categories = cats; },
      error: () => { this.toastService.showError('Greska pri ucitavanju kategorija.'); }
    });

    this.locationService.getAll().subscribe({
      next: (locs) => {
        this.locations = locs;
        this.filteredLocations = locs.slice(0, 30);
      },
      error: () => { this.toastService.showError('Greska pri ucitavanju lokacija.'); }
    });

    this.adService.getAdById(this.adId).subscribe({
      next: (ad) => {
        this.currentAd = ad;
        this.existingImages = [...(ad.images || [])];
        this.ensureLocationInList(ad.location);

        // Pre-fill location search display
        if (ad.location) {
          this.locationSearch = ad.location.municipality
            ? `${ad.location.city}, ${ad.location.municipality}`
            : ad.location.city;
        }

        this.form.patchValue({
          title: ad.title,
          description: ad.description,
          price: ad.price,
          currency: ad.currency,
          priceInterval: ad.priceInterval,
          categoryId: ad.category?.id ?? null,
          locationId: ad.location?.id ?? null,
          totalQuantity: ad.totalQuantity,
          images: this.existingImages,
          pricePerWeek: ad.pricePerWeek ?? null,
          pricePerMonth: ad.pricePerMonth ?? null,
          deposit: ad.deposit ?? null,
          advertiserType:       ad.advertiserType ?? null,
          roomCount:            ad.roomCount ?? null,
          areaSize:             ad.areaSize ?? null,
          constructionType:     ad.constructionType ?? null,
          propertyCondition:    ad.propertyCondition ?? null,
          totalFloors:          ad.totalFloors ?? null,
          floorNumber:          ad.floorNumber ?? null,
          furnished:            ad.furnished ?? null,
          heatingTypes:         ad.heatingTypes ?? [],
          propertyMunicipality: ad.propertyMunicipality ?? null,
          propertyNeighborhood: ad.propertyNeighborhood ?? null,
          propertyStreet:       ad.propertyStreet ?? null,
          landArea:             ad.landArea ?? null,
          landAreaUnit:         ad.landAreaUnit ?? 'ar',
          features:             ad.features ?? [],
          carBrand:            ad.carBrand ?? null,
          carModel:            ad.carModel ?? null,
          carYear:             ad.carYear ?? null,
          carMileage:          ad.carMileage ?? null,
          carBodyType:         ad.carBodyType ?? null,
          carFuelType:         ad.carFuelType ?? null,
          carTransmission:     ad.carTransmission ?? null,
          carPowerKw:          ad.carPowerKw ?? null,
          carColor:            ad.carColor ?? null,
          carDoors:            ad.carDoors ?? null,
          carSeats:            ad.carSeats ?? null,
          carDisplacement:     ad.carDisplacement ?? null,
          carEmissionClass:    ad.carEmissionClass ?? null,
          carDrive:            ad.carDrive ?? null,
          carSteeringWheel:    ad.carSteeringWheel ?? null,
          carRegisteredUntil:  ad.carRegisteredUntil ?? null,
          carCountry:          ad.carCountry ?? null,
          carOrigin:           ad.carOrigin ?? null,
          carOwnership:        ad.carOwnership ?? null,
          carDamage:           ad.carDamage ?? null,
          carLabel:            ad.carLabel ?? null,
          carInteriorMaterial: ad.carInteriorMaterial ?? null,
          carInteriorColor:    ad.carInteriorColor ?? null,
        });

        setTimeout(() => {
          if (this.descEditorRef?.nativeElement) {
            this.descEditorRef.nativeElement.innerHTML = ad.description ?? '';
          }
        });

        this.isLoading = false;
      },
      error: () => {
        this.toastService.showError('Ne mogu da ucitam oglas.');
        this.router.navigate(['/user/me/ads']);
      }
    });
  }

  // ════════════════════════════════════════════════════════
  //  NAVIGATION
  // ════════════════════════════════════════════════════════

  get stepProgress(): number {
    return Math.round(((this.currentStep - 1) / (this.totalSteps - 1)) * 100);
  }

  get stepValid(): boolean {
    switch (this.currentStep) {
      case 1:
        return !!this.form.get('categoryId')?.value
          && !this.form.get('title')?.invalid
          && !this.form.get('description')?.invalid
          && this.titleLength >= 5
          && this.descLength >= 20;
      case 2:
        return (this.existingImages.length > 0 || this.selectedFiles.length > 0)
          && !this.form.get('price')?.invalid
          && !!this.form.get('currency')?.value
          && !!this.form.get('locationId')?.value;
      default:
        return false;
    }
  }

  nextStep(): void {
    if (!this.stepValid) { this.markCurrentStepTouched(); return; }
    if (this.currentStep < this.totalSteps) this.currentStep++;
  }

  prevStep(): void {
    if (this.currentStep > 1) this.currentStep--;
  }

  goToStep(step: number): void {
    if (step < this.currentStep) this.currentStep = step;
  }

  private markCurrentStepTouched(): void {
    const map: Record<number, string[]> = {
      1: ['categoryId', 'title', 'description'],
      2: ['images', 'price', 'currency', 'locationId'],
    };
    (map[this.currentStep] ?? []).forEach(f => this.form.get(f)?.markAsTouched());
  }

  // ════════════════════════════════════════════════════════
  //  IMAGES (drag-drop)
  // ════════════════════════════════════════════════════════

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) this.addFiles(input.files);
    input.value = '';
  }

  onDragOver(e: DragEvent): void  { e.preventDefault(); e.stopPropagation(); this.isDragging = true;  }
  onDragLeave(e: DragEvent): void { e.preventDefault(); e.stopPropagation(); this.isDragging = false; }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging = false;
    if (e.dataTransfer?.files) this.addFiles(e.dataTransfer.files);
  }

  private async addFiles(files: FileList): Promise<void> {
    const totalExisting = this.existingImages.length + this.selectedFiles.length;
    const remaining = this.MAX_IMAGES - totalExisting;
    let added = 0;
    for (let i = 0; i < files.length && added < remaining; i++) {
      const file = files[i];
      const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
      const isHeic = ext === 'heic' || ext === 'heif';
      if (!file.type.match(/^image\//i) && !isHeic) { this.toastService.showError(`"${file.name}" nije slika.`); continue; }
      if (file.size > 10 * 1024 * 1024) { this.toastService.showError(`"${file.name}" premašuje 10MB.`); continue; }
      let fileToAdd = file;
      if (isHeic) {
        const converted = await this.convertHeic(file);
        if (!converted) { this.toastService.showError(`"${file.name}" ne može biti prikazan.`); continue; }
        fileToAdd = converted;
      }
      this.selectedFiles.push(fileToAdd);
      this.previewUrl.push(URL.createObjectURL(fileToAdd));
      added++;
    }
    this.syncImagesControl();
  }

  removeExistingImage(index: number): void {
    this.existingImages.splice(index, 1);
    this.syncImagesControl();
  }

  removeNewImage(index: number): void {
    URL.revokeObjectURL(this.previewUrl[index]);
    this.selectedFiles.splice(index, 1);
    this.previewUrl.splice(index, 1);
    this.syncImagesControl();
  }

  private syncImagesControl(): void {
    const combined = [...this.existingImages, ...this.previewUrl];
    this.form.patchValue({images: combined});
    this.form.get('images')?.updateValueAndValidity();
  }

  get totalImageCount(): number {
    return this.existingImages.length + this.selectedFiles.length;
  }

  // ════════════════════════════════════════════════════════
  //  LOCATION
  // ════════════════════════════════════════════════════════

  onLocationFocus(): void {
    this.showLocationDropdown = true;
    this.filteredLocations = this.locations.slice(0, 30);
    if (this.locTriggerRef) {
      const rect = this.locTriggerRef.nativeElement.getBoundingClientRect();
      this.locDropdownStyle = {
        top: (rect.bottom + 4) + 'px',
        left: rect.left + 'px',
        minWidth: rect.width + 'px',
        width: Math.max(rect.width, 480) + 'px'
      };
    }
  }

  @HostListener('document:mousedown', ['$event'])
  onOutsideLocClick(event: MouseEvent): void {
    if (!this.showLocationDropdown) return;
    const trigger = this.locTriggerRef?.nativeElement;
    if (trigger && !trigger.closest('.loc-wrapper')?.contains(event.target as Node)) {
      this.showLocationDropdown = false;
    }
  }

  onLocationInput(event: Event): void {
    const q = (event.target as HTMLInputElement).value;
    this.locationSearch = q;
    this.showLocationDropdown = true;
    this.form.patchValue({ locationId: null });
    const lower = q.toLowerCase();
    this.filteredLocations = (lower
      ? this.locations.filter(l =>
          l.city.toLowerCase().includes(lower) ||
          (l.municipality ?? '').toLowerCase().includes(lower))
      : this.locations
    ).slice(0, 30);
  }

  selectLocation(loc: Location): void {
    this.form.patchValue({ locationId: loc.id });
    this.locationSearch = loc.municipality ? `${loc.city}, ${loc.municipality}` : loc.city;
    this.showLocationDropdown = false;
  }

  hideLocationDropdown(): void { /* no-op, handled by mousedown */ }

  clearLocation(): void {
    this.form.patchValue({ locationId: null });
    this.locationSearch = '';
  }

  getSelectedLocationDisplay(): string {
    const id = this.form.get('locationId')?.value;
    if (!id) return '';
    const loc = this.locations.find(l => l.id === id);
    return loc ? (loc.municipality ? `${loc.city}, ${loc.municipality}` : loc.city) : '';
  }

  // ════════════════════════════════════════════════════════
  //  PRICING
  // ════════════════════════════════════════════════════════

  get priceDisplay(): string {
    const price = this.form.get('price')?.value;
    if (!price || price <= 0) return '—';
    const sym  = this.selectedCurrency === 'EUR' ? '€' : 'RSD';
    const unit = this.priceIntervalOptions.find(p => p.value === this.selectedInterval)?.unit ?? '';
    return `${Number(price).toLocaleString('sr-RS')} ${sym}${unit}`;
  }

  selectPriceInterval(value: PriceInterval): void {
    this.form.patchValue({ priceInterval: value });
  }

  // ════════════════════════════════════════════════════════
  //  QUANTITY
  // ════════════════════════════════════════════════════════

  increment(): void {
    const v = this.quantity;
    if (v < 999) this.form.patchValue({ totalQuantity: v + 1 });
  }

  decrement(): void {
    const v = this.quantity;
    if (v > 1) this.form.patchValue({ totalQuantity: v - 1 });
  }

  // ════════════════════════════════════════════════════════
  //  GETTERS
  // ════════════════════════════════════════════════════════

  private async convertHeic(file: File): Promise<File | null> {
    const url = URL.createObjectURL(file);
    const nativeOk = await new Promise<boolean>(res => {
      const img = new Image();
      img.onload = () => res(true);
      img.onerror = () => res(false);
      img.src = url;
    });
    URL.revokeObjectURL(url);
    if (nativeOk) return file;
    try {
      const mod = await import('heic2any');
      const fn: any = (mod as any).default ?? mod;
      const result = await fn({ blob: file, toType: 'image/jpeg', quality: 0.85 });
      const blob: Blob = Array.isArray(result) ? result[0] : result;
      return new File([blob], file.name.replace(/\.(heic|heif)$/i, '.jpg'), { type: 'image/jpeg' });
    } catch {
      return null;
    }
  }

  onDescInput(): void {
    const el = this.descEditorRef!.nativeElement;
    const text = el.innerText.replace(/^\s+|\s+$/g, '');
    this.form.get('description')?.setValue(text ? el.innerHTML : '', { emitEvent: false });
    this.form.get('description')?.markAsDirty();
  }

  onDescBlur(): void { this.form.get('description')?.markAsTouched(); }

  onDescKeydown(e: KeyboardEvent): void {
    if ((e.ctrlKey || e.metaKey) && e.key === 'b') { e.preventDefault(); this.execFormat('bold'); }
    if ((e.ctrlKey || e.metaKey) && e.key === 'i') { e.preventDefault(); this.execFormat('italic'); }
  }

  execFormat(cmd: 'bold' | 'italic'): void {
    document.execCommand(cmd, false);
    this.descEditorRef?.nativeElement.focus();
    this.onDescInput();
  }

  get titleLength(): number      { return this.form.get('title')?.value?.length ?? 0; }
  get descLength(): number {
    const raw = this.form.get('description')?.value ?? '';
    return raw.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').length;
  }
  get quantity(): number         { return this.form.get('totalQuantity')?.value ?? 1; }
  get selectedCurrency(): string { return this.form.get('currency')?.value ?? 'RSD'; }
  get selectedInterval(): string { return this.form.get('priceInterval')?.value ?? PriceInterval.PER_DAY; }

  private getRootCategoryId(catId: number | null): number | null {
    if (!catId) return null;
    let cat = this.categories.find(c => c.id === catId);
    while (cat?.parentId) {
      cat = this.categories.find(c => c.id === cat!.parentId);
    }
    return cat?.id ?? null;
  }

  get isRealEstate(): boolean {
    return this.getRootCategoryId(this.form.get('categoryId')?.value) === this.RE_ROOT_ID;
  }

  get reSubcategoryName(): string | null {
    const catId = this.form.get('categoryId')?.value;
    if (!catId) return null;
    const cat = this.categories.find(c => c.id === catId);
    if (!cat) return null;
    if (cat.parentId === this.RE_ROOT_ID) return cat.name;
    if (cat.parentId) {
      const parent = this.categories.find(c => c.id === cat.parentId);
      if (parent?.parentId === this.RE_ROOT_ID) return parent.name;
    }
    return null;
  }

  get isHouse(): boolean { return this.reSubcategoryName === 'Kuće'; }
  get isGarageParking(): boolean { return this.reSubcategoryName === 'Garaže i parking mesta'; }
  get isCommercial(): boolean { return this.reSubcategoryName === 'Poslovni prostor'; }

  get vehicleSubcategoryName(): string | null {
    const catId = this.form.get('categoryId')?.value;
    if (!catId) return null;
    const cat = this.categories.find(c => c.id === catId);
    if (!cat) return null;
    if (cat.parentId === this.VEHICLE_ROOT_ID) return cat.name;
    if (cat.parentId) {
      const parent = this.categories.find(c => c.id === cat.parentId);
      if (parent?.parentId === this.VEHICLE_ROOT_ID) return parent.name;
    }
    return null;
  }
  get isCar(): boolean { return this.vehicleSubcategoryName === 'Automobili'; }

  toggleHeating(value: string): void {
    const curr: string[] = [...(this.form.get('heatingTypes')?.value ?? [])];
    const idx = curr.indexOf(value);
    if (idx >= 0) curr.splice(idx, 1); else curr.push(value);
    this.form.patchValue({ heatingTypes: curr });
  }

  isHeatingSelected(value: string): boolean {
    return (this.form.get('heatingTypes')?.value ?? []).includes(value);
  }

  toggleFeature(value: string): void {
    const curr: string[] = [...(this.form.get('features')?.value ?? [])];
    const idx = curr.indexOf(value);
    if (idx >= 0) curr.splice(idx, 1); else curr.push(value);
    this.form.patchValue({ features: curr });
  }

  isFeatureSelected(value: string): boolean {
    return (this.form.get('features')?.value ?? []).includes(value);
  }

  private ensureLocationInList(location?: Location | null): void {
    if (!location) return;
    const exists = this.locations.some(loc => loc.id === location.id);
    if (!exists) {
      this.locations = [...this.locations, location];
    }
  }

  // ════════════════════════════════════════════════════════
  //  SUBMIT
  // ════════════════════════════════════════════════════════

  ngOnDestroy(): void {
    this.previewUrl.forEach(url => URL.revokeObjectURL(url));
  }

  onSubmit(): void {
    if (this.isSubmitting) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja.');
      return;
    }
    this.isSubmitting = true;

    const basePayload: Omit<UpdateAdRequest, 'images'> = {
      title: this.form.value.title,
      description: this.form.value.description,
      price: this.form.value.price,
      currency: this.form.value.currency,
      priceInterval: this.form.value.priceInterval,
      totalQuantity: this.form.value.totalQuantity,
      categoryId: this.form.value.categoryId,
      locationId: this.form.value.locationId,
      pricePerWeek: this.form.value.pricePerWeek,
      pricePerMonth: this.form.value.pricePerMonth,
      deposit: this.form.value.deposit,
      advertiserType:       this.form.value.advertiserType,
      roomCount:            this.form.value.roomCount,
      areaSize:             this.form.value.areaSize,
      constructionType:     this.form.value.constructionType,
      propertyCondition:    this.form.value.propertyCondition,
      totalFloors:          this.form.value.totalFloors,
      floorNumber:          this.form.value.floorNumber,
      furnished:            this.form.value.furnished,
      heatingTypes:         this.form.value.heatingTypes,
      propertyMunicipality: this.form.value.propertyMunicipality,
      propertyNeighborhood: this.form.value.propertyNeighborhood,
      propertyStreet:       this.form.value.propertyStreet,
      landArea:             this.form.value.landArea,
      landAreaUnit:         this.form.value.landAreaUnit,
      features:             this.form.value.features,
      carBrand:            this.form.value.carBrand,
      carModel:            this.form.value.carModel,
      carYear:             this.form.value.carYear,
      carMileage:          this.form.value.carMileage,
      carBodyType:         this.form.value.carBodyType,
      carFuelType:         this.form.value.carFuelType,
      carTransmission:     this.form.value.carTransmission,
      carPowerKw:          this.form.value.carPowerKw,
      carColor:            this.form.value.carColor,
      carDoors:            this.form.value.carDoors,
      carSeats:            this.form.value.carSeats,
      carDisplacement:     this.form.value.carDisplacement,
      carEmissionClass:    this.form.value.carEmissionClass,
      carDrive:            this.form.value.carDrive,
      carSteeringWheel:    this.form.value.carSteeringWheel,
      carRegisteredUntil:  this.form.value.carRegisteredUntil,
      carCountry:          this.form.value.carCountry,
      carOrigin:           this.form.value.carOrigin,
      carOwnership:        this.form.value.carOwnership,
      carDamage:           this.form.value.carDamage,
      carLabel:            this.form.value.carLabel,
      carInteriorMaterial: this.form.value.carInteriorMaterial,
      carInteriorColor:    this.form.value.carInteriorColor,
    };

    const finalizeUpdate = (images: string[]) => {
      if (images.length === 0) {
        this.isSubmitting = false;
        this.toastService.showError('Morate ostaviti bar jednu sliku.');
        return;
      }

      const payload: UpdateAdRequest = {
        ...basePayload,
        images
      };

      this.adService.updateAd(this.adId, payload).subscribe({
        next: (updatedAd) => {
          this.toastService.showSuccess('Oglas uspesno izmenjen.');
          this.router.navigate(['/ads', updatedAd.id]);
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.showError('Greska pri izmeni oglasa.');
        }
      });
    };

    if (this.selectedFiles.length > 0) {
      this.adService.uploadImages(this.selectedFiles).subscribe({
        next: (uploadedUrls) => {
          finalizeUpdate([...this.existingImages, ...uploadedUrls]);
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.showError('Greska pri upload-u slika.');
        }
      });
    } else {
      finalizeUpdate([...this.existingImages]);
    }
  }
}
