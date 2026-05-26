import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdService } from '../../services/ad.service';
import { Category } from '../../../../shared/models/category.model';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PriceInterval } from '../../../../shared/models/price-interval.enum';
import { CategoryService } from '../../services/category.service';
import { LocationService } from '../../services/location.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, filter, Subject, switchMap, take, takeUntil } from 'rxjs';
import { ToastService } from '../../../../shared/services/toast.service';
import { Location } from '../../../../shared/models/location.model';
import { CityPickerComponent, CityPickerOption } from '../../../../shared/components/city-picker/city-picker.component';
import { AuthService } from '../../../auth/services/auth.service';
import { AdTemplate, AdTemplateService } from '../../services/ad-template.service';


@Component({
  selector: 'app-create-ad',
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink, CityPickerComponent],
  standalone: true,
  templateUrl: './create-ad.component.html',
  styleUrl: './create-ad.component.css'
})
export class CreateAdComponent implements OnInit, OnDestroy {

  // ── Step state ──────────────────────────────────────────────────────────
  currentStep = 1;
  readonly totalSteps = 2;
  isSubmitting = false;

  // ── Template state ──────────────────────────────────────────────────────
  saveTemplateModalOpen = false;
  templateName = '';
  isSavingTemplate = false;

  // ── Images ──────────────────────────────────────────────────────────────
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  isDragging = false;
  readonly MAX_IMAGES = 10;

  // ── Categories ──────────────────────────────────────────────────────────
  categories: Category[] = [];
  parentCategories: Category[] = [];
  childCategories: Category[] = [];
  grandchildCategories: Category[] = [];
  selectedParentId: number | null = null;
  selectedLevel2Id: number | null = null;

  // ── Category suggest ─────────────────────────────────────────────────────
  suggestedCategories: Category[] = [];
  isSuggestingCategories = false;

  // ── Real estate ──────────────────────────────────────────────────────────
  readonly RE_ROOT_ID = 900;

  readonly reAdvertiserOptions = ['AGENCIJA', 'VLASNIK', 'INVESTITOR'];
  readonly reAdvertiserLabels: Record<string, string> = {
    AGENCIJA: 'Agencija', VLASNIK: 'Vlasnik', INVESTITOR: 'Investitor'
  };

  readonly reRoomOptions = [
    { value: '0.5', label: 'Garsonjera' },
    { value: '1.0', label: 'Jednosoban' },
    { value: '1.5', label: 'Jednoiposoban' },
    { value: '2.0', label: 'Dvosoban' },
    { value: '2.5', label: 'Dvoiposoban' },
    { value: '3.0', label: 'Trosoban' },
    { value: '3.5', label: 'Troiposoban' },
    { value: '4.0', label: 'Četvorosoban' },
    { value: '4.5', label: 'Četvoroiposoban' },
    { value: '5+',  label: 'Petosoban i veći' },
  ];

  readonly reConstructionOptions = [
    { value: 'NOVOGRADNJA', label: 'Novogradnja' },
    { value: 'STARA_GRADNJA', label: 'Stara gradnja' },
  ];

  readonly reConditionOptions = [
    { value: 'IZVORNO_STANJE',      label: 'Izvorno stanje' },
    { value: 'U_IZGRADNJI',         label: 'U izgradnji' },
    { value: 'RENOVIRANO',          label: 'Renovirano' },
    { value: 'POTREBNO_RENOVIRANJE', label: 'Potrebno renoviranje' },
    { value: 'LUKSUZNO',            label: 'Luksuzno' },
  ];

  readonly reTotalFloorOptions: { value: string; label: string }[] = (() => {
    const opts = [];
    for (let i = 1; i <= 30; i++) {
      const lbl = i === 1 ? '1 sprat' : i < 5 ? `${i} sprata` : `${i} spratova`;
      opts.push({ value: String(i), label: lbl });
    }
    opts.push({ value: '30+', label: '30+ spratova' });
    return opts;
  })();

  readonly reFloorOptions: { value: string; label: string }[] = (() => {
    const specials = [
      { value: 'PODRUM',          label: 'Podrum' },
      { value: 'SUTEREN',         label: 'Suteren' },
      { value: 'NISKO_PRIZEMLJE', label: 'Nisko prizemlje' },
      { value: 'PRIZEMLJE',       label: 'Prizemlje' },
      { value: 'VISOKO_PRIZEMLJE', label: 'Visoko prizemlje' },
      { value: 'POTKROVLJE',      label: 'Potkrovlje' },
    ];
    const numbered = [];
    for (let i = 1; i <= 30; i++) {
      numbered.push({ value: String(i), label: `${i}. sprat` });
    }
    numbered.push({ value: '30+', label: '30+. sprat' });
    return [...specials, ...numbered];
  })();

  readonly reFurnishedOptions = [
    { value: 'NAMESTENO',     label: 'Namešteno' },
    { value: 'POLUNAMESTENO', label: 'Polunaměšteno' },
    { value: 'PRAZNO',        label: 'Prazno' },
  ];

  readonly reHeatingOptions = [
    { value: 'CENTRALNO',           label: 'Centralno' },
    { value: 'KLIMA',               label: 'Klima' },
    { value: 'ETAZNO',              label: 'Etažno' },
    { value: 'TOPLOTNA_PUMPA',      label: 'Toplotna pumpa' },
    { value: 'STRUJA',              label: 'Struja' },
    { value: 'GAS',                 label: 'Gas' },
    { value: 'MERMERNI_RADIJATORI', label: 'Mermerni radijatori' },
    { value: 'NORVESKI_RADIJATORI', label: 'Norveški radijatori' },
    { value: 'CVRSTO_GORIVO',       label: 'Čvrsto gorivo' },
    { value: 'TA_PEC',              label: 'TA peć' },
  ];

  readonly reHouseRoomOptions = [
    { value: '1',     label: '1 soba' },
    { value: '2',     label: '2 sobe' },
    { value: '3',     label: '3 sobe' },
    { value: '4',     label: '4 sobe' },
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
    { value: 'LIMUZINA',     label: 'Limuzina' },
    { value: 'HECBEK',       label: 'Hečbek' },
    { value: 'KAR',          label: 'Karavan' },
    { value: 'SUV',          label: 'SUV / Džip' },
    { value: 'MONOVOLUMEN',  label: 'Monovolumen' },
    { value: 'KABRIOLET',    label: 'Kabriolet' },
    { value: 'KUPE',         label: 'Kupe' },
    { value: 'PIKAP',        label: 'Pikap' },
    { value: 'KOMBI',        label: 'Kombi' },
    { value: 'OSTALO',       label: 'Ostalo' },
  ];
  readonly carFuelTypeOptions = [
    { value: 'BENZIN',     label: 'Benzin' },
    { value: 'DIZEL',      label: 'Dizel' },
    { value: 'TNG',        label: 'TNG' },
    { value: 'METAN',      label: 'Metan (CNG)' },
    { value: 'HIBRID',     label: 'Hibrid' },
    { value: 'ELEKTRICNI', label: 'Električni' },
    { value: 'VODIK',      label: 'Vodik' },
  ];
  readonly carTransmissionOptions = [
    { value: 'MANUELNI',       label: 'Manuelni' },
    { value: 'AUTOMATSKI',     label: 'Automatski' },
    { value: 'POLUAUTOMATSKI', label: 'Poluautomatski' },
  ];
  readonly carDriveOptions = [
    { value: 'PREDNJI', label: 'Prednji pogon' },
    { value: 'ZADNJI',  label: 'Zadnji pogon' },
    { value: '4X4',     label: '4x4 / AWD' },
  ];
  readonly carDoorsOptions = [
    { value: '2/3', label: '2/3 vrata' },
    { value: '4/5', label: '4/5 vrata' },
  ];
  readonly carSteeringWheelOptions = [
    { value: 'LEVI',  label: 'Levi volan' },
    { value: 'DESNI', label: 'Desni volan' },
  ];
  readonly carOriginOptions = [
    { value: 'DOMACE', label: 'Domaće' },
    { value: 'UVOZ',   label: 'Uvoz' },
    { value: 'IZ_EU',  label: 'Iz EU' },
  ];
  readonly carOwnershipOptions = [
    { value: 'PRIVATNO', label: 'Privatno' },
    { value: 'FIRMA',    label: 'Firma' },
    { value: 'STRANO',   label: 'Strano' },
  ];
  readonly carDamageOptions = [
    { value: 'NEOSTECAN',         label: 'Neoštećen' },
    { value: 'OSTECEN_VOZNO',     label: 'Oštećen — vozno' },
    { value: 'OSTECEN_NEVOZNO',   label: 'Oštećen — nevozno' },
    { value: 'RASHODOVANO',       label: 'Rashodovano' },
  ];
  readonly carColorOptions = [
    { value: 'CRNA',        label: 'Crna' },
    { value: 'BELA',        label: 'Bela' },
    { value: 'SIVA',        label: 'Siva' },
    { value: 'CRVENA',      label: 'Crvena' },
    { value: 'PLAVA',       label: 'Plava' },
    { value: 'ZELENA',      label: 'Zelena' },
    { value: 'ZUTA',        label: 'Žuta' },
    { value: 'NARANDZASTA', label: 'Narandžasta' },
    { value: 'SMEDA',       label: 'Smeđa' },
    { value: 'BORDO',       label: 'Bordo' },
    { value: 'ZLATNA',      label: 'Zlatna' },
    { value: 'SREBRNA',     label: 'Srebrna' },
    { value: 'OSTALO',      label: 'Ostalo' },
  ];
  readonly carEmissionOptions = [
    { value: 'EURO1', label: 'Euro 1' }, { value: 'EURO2', label: 'Euro 2' },
    { value: 'EURO3', label: 'Euro 3' }, { value: 'EURO4', label: 'Euro 4' },
    { value: 'EURO5', label: 'Euro 5' }, { value: 'EURO6', label: 'Euro 6' },
  ];
  readonly carInteriorMaterialOptions = [
    { value: 'TKANINA',       label: 'Tkanina' },
    { value: 'KOZA',          label: 'Koža' },
    { value: 'VESTACKA_KOZA', label: 'Veštačka koža' },
    { value: 'KOMBINOVANO',   label: 'Kombinovano' },
  ];
  readonly carInteriorColorOptions = [
    { value: 'CRNA',  label: 'Crna' },
    { value: 'SIVA',  label: 'Siva' },
    { value: 'BELA',  label: 'Bela' },
    { value: 'SMEDA', label: 'Smeđa' },
    { value: 'BEZ',   label: 'Bež' },
    { value: 'BORDO', label: 'Bordo' },
  ];
  readonly carLabelOptions = [
    { value: 'A', label: 'A' }, { value: 'B', label: 'B' }, { value: 'C', label: 'C' },
    { value: 'D', label: 'D' }, { value: 'E', label: 'E' }, { value: 'F', label: 'F' },
    { value: 'G', label: 'G' },
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
    { value: 'ODMAH_USELJIVO',    label: 'Odmah useljivo' },
    { value: 'PODRUM',            label: 'Podrum' },
    { value: 'VIDEO_NADZOR',      label: 'Video nadzor' },
    { value: 'POMOCNI_OBJEKTI',   label: 'Pomoćni objekti' },
    { value: 'VODA',              label: 'Voda' },
    { value: 'INTERNET',          label: 'Internet' },
    { value: 'INTERFON',          label: 'Interfon' },
    { value: 'TERASA',            label: 'Terasa' },
    { value: 'KABLOVA_TV',        label: 'Kablovska TV' },
    { value: 'KLIMA_UREDJAJ',     label: 'Klima' },
    { value: 'ENERGETSKI_PASOS',  label: 'Energetski pasoš' },
    { value: 'PRISTUP_INVALIDIMA', label: 'Prilaz za invalide' },
    { value: 'PARKING',           label: 'Parking' },
    { value: 'GARAZA',            label: 'Garaža' },
    { value: 'BAZEN',             label: 'Bazen' },
    { value: 'KANALIZACIJA',      label: 'Kanalizacija' },
    { value: 'STRUJA_PRIKLJUCAK', label: 'Struja' },
    { value: 'ASFALTIRAN_PRILAZ', label: 'Asfaltiran prilaz' },
    { value: 'PET_FRIENDLY',      label: 'Pet friendly' },
    { value: 'DEPOZIT',           label: 'Depozit' },
  ];

  @ViewChild('descEditor') descEditorRef?: ElementRef<HTMLDivElement>;

  private destroy$ = new Subject<void>();

  // ── Locations ───────────────────────────────────────────────────────────
  locations: Location[] = [];
  initialLocationId: number | null = null;

  get userCity(): string | null {
    const userLocId = this.authService.currentUserValue?.locationId;
    if (!userLocId || !this.locations.length) return null;
    return this.locations.find(l => l.id === userLocId)?.city ?? null;
  }

  // ── Form ────────────────────────────────────────────────────────────────
  form!: FormGroup;
  readonly MAX_TITLE = 100;
  readonly MAX_DESC = 2000;

  readonly priceIntervalOptions = [
    { value: PriceInterval.PER_HOUR,  label: 'Po satu',   unit: '/h'   },
    { value: PriceInterval.PER_DAY,   label: 'Po danu',   unit: '/dan' },
    { value: PriceInterval.PER_MONTH, label: 'Po mesecu', unit: '/mes' },
  ];

  readonly stepConfig = [
    { label: 'Kategorija' },
    { label: 'Detalji oglasa' },
  ];

  private readonly catIconMap: Record<string, string> = {
    alat: 'build', bušil: 'build', mašin: 'settings',
    vozil: 'directions_car', automobil: 'directions_car', motor: 'two_wheeler', bicikl: 'pedal_bike',
    elektronika: 'laptop', kompjuter: 'laptop', telefon: 'smartphone',
    sport: 'sports_soccer', fitnes: 'fitness_center', ski: 'downhill_skiing',
    muzik: 'music_note', instrument: 'music_note',
    knjig: 'menu_book',
    kuhin: 'kitchen',
    namešt: 'chair',
    bašta: 'tools_power_drill',
    kuća: 'home',
    proslave: 'celebration',
    nekretnine: 'home_work',
    kamp: 'landscape', planin: 'landscape',
    foto: 'photo_camera', kamera: 'photo_camera', video: 'videocam',
    garderob: 'checkroom', venčanic: 'checkroom', halj: 'checkroom',
    odelo: 'checkroom', smoking: 'checkroom', kostim: 'checkroom',
    nošnj: 'checkroom', obuć: 'checkroom', torbic: 'checkroom',
  };

  constructor(
    private adService: AdService,
    private categoryService: CategoryService,
    private locationService: LocationService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService,
    private adTemplateService: AdTemplateService,
  ) {}

  ngOnInit(): void {
    this.categoryService.getAll().subscribe({
      next: cats => {
        this.categories = cats;
        this.parentCategories = cats.filter(c => !c.parentId);
        this.tryApplyTemplateFromUrl();
      },
      error: () => this.toastService.showError('Greška pri učitavanju kategorija.'),
    });

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = Number(params['template']);
      if (id && this.categories.length) this.applyTemplateById(id);
    });

    this.locationService.getAll().subscribe({
      next: locs => { this.locations = locs; },
      error: () => this.toastService.showError('Greška pri učitavanju lokacija.'),
    });

    this.authService.currentUser$.pipe(
      filter(u => u !== null),
      take(1)
    ).subscribe(user => {
      if (user?.locationId && !this.initialLocationId) {
        this.initialLocationId = user.locationId;
      }
    });

    this.form = this.fb.group({
      title:         ['', [Validators.required, Validators.minLength(5), Validators.maxLength(this.MAX_TITLE)]],
      description:   ['', [
        (c: AbstractControl) => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length > 0 ? null : { required: true }; },
        (c: AbstractControl) => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length >= 20 ? null : { minlength: { requiredLength: 20 } }; },
        (c: AbstractControl) => { const t = (c.value ?? '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim(); return t.length <= this.MAX_DESC ? null : { maxlength: { requiredLength: this.MAX_DESC } }; },
      ]],
      price:         [null, [Validators.required, Validators.min(1)]],
      currency:      ['RSD', Validators.required],
      priceInterval: [PriceInterval.PER_DAY, Validators.required],
      categoryId:    [null, Validators.required],
      locationId:    [null, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1), Validators.max(999)]],
      images:        [[]],
      pricePerWeek:  [null],
      pricePerMonth: [null],
      deposit:       [null, [Validators.min(0)]],
      // Real estate
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
      // Car fields
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
    this.form.get('title')?.valueChanges.pipe(
      debounceTime(800),
      distinctUntilChanged(),
      filter(title => title && title.length > 5),
      switchMap(title => this.categoryService.suggestCategory(title)),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (categoryIds) => {
        if (categoryIds?.length) {
          const found = this.categories.find(c => c.id === categoryIds[0]);
          if (found) {
            this.applySuggestedCategory(found);
            this.toastService.showSuccess('AI je automatski prepoznao kategoriju!');
          }
        }
      },
      error: () => {},
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
      case 1: return !!this.form.get('categoryId')?.value;
      case 2: return this.selectedFiles.length > 0
                     && !this.form.get('title')?.invalid
                     && !this.form.get('description')?.invalid
                     && !this.form.get('price')?.invalid
                     && !!this.form.get('currency')?.value
                     && !!this.form.get('locationId')?.value;
      default: return false;
    }
  }

  nextStep(): void {
    if (!this.stepValid) { this.markCurrentStepTouched(); return; }
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
      window.scrollTo(0, 0);
      if (this.currentStep === 2) setTimeout(() => this.populateDescEditor());
    }
  }

  private populateDescEditor(): void {
    const html = this.form.get('description')?.value;
    if (this.descEditorRef && html && !this.descEditorRef.nativeElement.innerHTML.trim()) {
      this.descEditorRef.nativeElement.innerHTML = html;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) { this.currentStep--; window.scrollTo(0, 0); }
  }

  goToStep(step: number): void {
    if (step < this.currentStep) this.currentStep = step;
  }

  private markCurrentStepTouched(): void {
    const map: Record<number, string[]> = {
      1: ['categoryId'],
      2: ['title', 'description', 'images', 'price', 'currency', 'locationId'],
    };
    (map[this.currentStep] ?? []).forEach(f => this.form.get(f)?.markAsTouched());
  }

  // ════════════════════════════════════════════════════════
  //  CATEGORIES — 3 levels
  // ════════════════════════════════════════════════════════

  getCategoryIcon(name: string): string {
    const lower = name.toLowerCase();
    for (const [key, icon] of Object.entries(this.catIconMap)) {
      if (lower.includes(key)) return icon;
    }
    return 'inventory_2';
  }

  selectParentCategory(cat: Category): void {
    this.selectedParentId = cat.id;
    this.selectedLevel2Id = null;
    this.childCategories = this.categories.filter(c => c.parentId === cat.id);
    this.grandchildCategories = [];
    if (this.childCategories.length === 0) {
      this.form.patchValue({ categoryId: cat.id });
    } else {
      this.form.patchValue({ categoryId: null });
    }
  }

  selectChildCategory(cat: Category): void {
    this.selectedLevel2Id = cat.id;
    this.grandchildCategories = this.categories.filter(c => c.parentId === cat.id);
    if (this.grandchildCategories.length === 0) {
      this.form.patchValue({ categoryId: cat.id });
    } else {
      this.form.patchValue({ categoryId: null });
    }
  }

  selectGrandchildCategory(cat: Category): void {
    this.form.patchValue({ categoryId: cat.id });
  }

  isParentSelected(cat: Category): boolean   { return this.selectedParentId === cat.id; }
  isChildSelected(cat: Category): boolean    { return this.selectedLevel2Id === cat.id; }
  isGrandchildSelected(cat: Category): boolean { return this.form.get('categoryId')?.value === cat.id; }

  getCategoryPath(cat: Category): string {
    const parts: string[] = [cat.name];
    let current: Category | undefined = cat;
    while (current?.parentId) {
      current = this.categories.find(c => c.id === current!.parentId);
      if (current) parts.unshift(current.name);
    }
    return parts.join(' › ');
  }

  // ════════════════════════════════════════════════════════
  //  CATEGORY SUGGEST
  // ════════════════════════════════════════════════════════

  get canSuggest(): boolean {
    return (this.form.get('title')?.value?.trim()?.length ?? 0) >= 3;
  }

  suggestCategories(): void {
    const title = this.form.get('title')?.value?.trim();
    if (!title || title.length < 3) return;
    this.isSuggestingCategories = true;
    this.suggestedCategories = [];
    this.categoryService.suggestCategory(title).subscribe({
      next: categoryIds => {
        this.suggestedCategories = (categoryIds ?? [])
          .map(id => this.categories.find(c => c.id === id))
          .filter((c): c is Category => !!c);
        this.isSuggestingCategories = false;
      },
      error: () => {
        this.isSuggestingCategories = false;
        this.toastService.showError('Greška pri preporuci kategorije.');
      },
    });
  }

  applySuggestedCategory(cat: Category): void {
    this.suggestedCategories = [];

    // Build chain: find level-1 and level-2 parents
    const level3 = !cat.parentId ? null : (() => {
      const parent = this.categories.find(c => c.id === cat.parentId);
      return parent?.parentId ? cat : null;
    })();

    const level2 = level3
      ? this.categories.find(c => c.id === level3.parentId) ?? null
      : (!cat.parentId ? null : cat);

    const level1 = level2
      ? (this.categories.find(c => c.id === level2.parentId) ?? (level2.parentId ? null : level2))
      : cat;

    if (level1) this.selectParentCategory(level1);
    if (level2 && level2.id !== level1?.id) this.selectChildCategory(level2);
    if (level3) this.selectGrandchildCategory(level3);
  }

  // ════════════════════════════════════════════════════════
  //  IMAGES
  // ════════════════════════════════════════════════════════

  onFileSelected(event: Event): void {
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

  private addFiles(files: FileList): void {
    const remaining = this.MAX_IMAGES - this.selectedFiles.length;
    let added = 0;
    for (let i = 0; i < files.length && added < remaining; i++) {
      const file = files[i];
      const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
      const isHeic = ext === 'heic' || ext === 'heif';
      if (!file.type.match(/^image\//i) && !isHeic) { this.toastService.showError(`"${file.name}" nije slika.`); continue; }
      if (file.size > 10 * 1024 * 1024) { this.toastService.showError(`"${file.name}" premašuje 10MB.`); continue; }
      this.selectedFiles.push(file);
      this.previewUrls.push(isHeic ? '__heic__' : URL.createObjectURL(file));
      added++;
    }
    this.form.patchValue({ images: this.previewUrls });
  }

  removeImage(index: number): void {
    if (this.previewUrls[index] !== '__heic__') URL.revokeObjectURL(this.previewUrls[index]);
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
    this.form.patchValue({ images: this.previewUrls });
  }

  setCoverImage(index: number): void {
    if (index === 0) return;
    const [f] = this.selectedFiles.splice(index, 1);
    const [u] = this.previewUrls.splice(index, 1);
    this.selectedFiles.unshift(f);
    this.previewUrls.unshift(u);
    this.form.patchValue({ images: this.previewUrls });
  }

  // ════════════════════════════════════════════════════════
  //  LOCATION
  // ════════════════════════════════════════════════════════

  onLocationChange(option: CityPickerOption | null): void {
    this.form.patchValue({ locationId: option?.locationId ?? null });
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
  //  SUBMIT
  // ════════════════════════════════════════════════════════

  onSubmit(): void {
    if (this.form.invalid || this.selectedFiles.length === 0) {
      this.form.markAllAsTouched();
      this.toastService.showError('Molimo popunite sva polja.');
      return;
    }
    this.isSubmitting = true;
    this.adService.uploadImages(this.selectedFiles).pipe(
      switchMap(urls => {
        this.form.patchValue({ images: urls });
        return this.adService.createAd(this.form.value);
      })
    ).subscribe({
      next: (ad) => {
        this.isSubmitting = false;
        this.toastService.showSuccess('Oglas uspešno kreiran!');
        this.router.navigate(['/ads', ad.id]);
      },
      error: () => {
        this.isSubmitting = false;
        this.toastService.showError('Greška pri kreiranju oglasa. Pokušajte ponovo.');
      },
    });
  }

  // ════════════════════════════════════════════════════════
  //  TEMPLATES
  // ════════════════════════════════════════════════════════

  /** Polja koja se snimaju u šablon (sve osim slika i runtime stanja). */
  private readonly TEMPLATE_FIELDS = [
    'title', 'description', 'price', 'currency', 'priceInterval',
    'categoryId', 'locationId', 'totalQuantity',
    'pricePerWeek', 'pricePerMonth', 'deposit',
    'advertiserType', 'roomCount', 'areaSize', 'constructionType', 'propertyCondition',
    'totalFloors', 'floorNumber', 'furnished', 'heatingTypes',
    'propertyMunicipality', 'propertyNeighborhood', 'propertyStreet',
    'landArea', 'landAreaUnit', 'features',
    'carBrand', 'carModel', 'carYear', 'carMileage', 'carBodyType', 'carFuelType',
    'carTransmission', 'carPowerKw', 'carColor', 'carDoors', 'carSeats',
    'carDisplacement', 'carEmissionClass', 'carDrive', 'carSteeringWheel',
    'carRegisteredUntil', 'carCountry', 'carOrigin', 'carOwnership',
    'carDamage', 'carLabel', 'carInteriorMaterial', 'carInteriorColor',
  ];

  private tryApplyTemplateFromUrl(): void {
    const id = Number(this.route.snapshot.queryParamMap.get('template'));
    if (id) this.applyTemplateById(id);
  }

  private applyTemplateById(id: number): void {
    this.adTemplateService.ensureLoaded();
    const cached = this.adTemplateService.get(id);
    if (cached) {
      this.applyTemplate(cached);
      return;
    }
    // Sačekaj da se lista učita pa probaj ponovo
    this.adTemplateService.list$.pipe(
      filter(list => list.length > 0),
      take(1),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      const t = this.adTemplateService.get(id);
      if (t) this.applyTemplate(t);
    });
  }

  private applyTemplate(t: AdTemplate): void {
    const data = t.data ?? {};
    const patch: { [key: string]: any } = {};
    for (const field of this.TEMPLATE_FIELDS) {
      if (data[field] !== undefined) patch[field] = data[field];
    }
    this.form.patchValue(patch);

    // Postavi HTML opisa u contenteditable polje (ako je view već renderovan)
    setTimeout(() => {
      if (this.descEditorRef && data['description']) {
        this.descEditorRef.nativeElement.innerHTML = data['description'];
      }
    });

    // Podesi kategorijsko stablo da reflektuje izabranu kategoriju
    const catId = data['categoryId'];
    if (catId) {
      const cat = this.categories.find(c => c.id === catId);
      if (cat) this.applySuggestedCategory(cat);
    }

    // Lokacija — postaviti initialLocationId da CityPicker pokaže izabran grad
    if (data['locationId']) {
      this.initialLocationId = data['locationId'];
    }

    this.toastService.showSuccess(`Šablon "${t.name}" učitan.`);
  }

  openSaveTemplateModal(): void {
    this.templateName = this.form.get('title')?.value?.toString().slice(0, 80) ?? '';
    this.saveTemplateModalOpen = true;
  }

  closeSaveTemplateModal(): void {
    this.saveTemplateModalOpen = false;
    this.templateName = '';
    this.isSavingTemplate = false;
  }

  saveAsTemplate(): void {
    const name = this.templateName.trim();
    if (!name) {
      this.toastService.showError('Unesite naziv šablona.');
      return;
    }
    if (!this.form.get('categoryId')?.value) {
      this.toastService.showError('Izaberite kategoriju pre čuvanja šablona.');
      return;
    }

    const data: { [key: string]: any } = {};
    for (const field of this.TEMPLATE_FIELDS) {
      const v = this.form.get(field)?.value;
      if (v !== null && v !== undefined && v !== '') data[field] = v;
    }

    this.isSavingTemplate = true;
    this.adTemplateService.create({ name, data }).subscribe({
      next: () => {
        this.toastService.showSuccess(`Šablon "${name}" sačuvan.`);
        this.closeSaveTemplateModal();
      },
      error: (err) => {
        this.isSavingTemplate = false;
        const msg = err?.error?.error ?? 'Greška pri čuvanju šablona.';
        this.toastService.showError(msg);
      },
    });
  }

  // ════════════════════════════════════════════════════════
  //  REAL ESTATE
  // ════════════════════════════════════════════════════════

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
    if (this.selectedParentId !== this.RE_ROOT_ID) return null;
    return this.categories.find(c => c.id === this.selectedLevel2Id)?.name ?? null;
  }

  get isHouse(): boolean { return this.reSubcategoryName === 'Kuće'; }
  get isGarageParking(): boolean { return this.reSubcategoryName === 'Garaže i parking mesta'; }
  get isCommercial(): boolean { return this.reSubcategoryName === 'Poslovni prostor'; }

  get vehicleSubcategoryName(): string | null {
    if (this.selectedParentId !== this.VEHICLE_ROOT_ID) return null;
    return this.categories.find(c => c.id === this.selectedLevel2Id)?.name ?? null;
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

  // ════════════════════════════════════════════════════════
  //  GETTERS
  // ════════════════════════════════════════════════════════

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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.previewUrls.filter(u => u !== '__heic__').forEach(url => URL.revokeObjectURL(url));
  }
}
