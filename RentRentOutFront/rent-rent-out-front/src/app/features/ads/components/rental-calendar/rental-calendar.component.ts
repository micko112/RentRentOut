import {Component, Input, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import {Subject, switchMap, tap, takeUntil} from 'rxjs';
import {CommonModule, DatePipe} from '@angular/common';
import {Ad} from '../../../../shared/models/ad.model';
import {CalendarDay} from '../../../../shared/models/day.model';
import {ContractService} from '../../../contracts/services/contract.service';
import {ToastService} from '../../../../shared/services/toast.service';
import {AdService} from '../../services/ad.service';
import {Router} from '@angular/router';
import {CreateRentalContractRequest} from '../../../../shared/models/create-rental-contract-request';
import {AuthService} from '../../../auth/services/auth.service';

@Component({
  selector: 'app-rental-calendar',
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe],
  templateUrl: './rental-calendar.component.html',
  styleUrl: './rental-calendar.component.css'
})
export class RentalCalendarComponent implements OnChanges, OnDestroy {

  private destroy$ = new Subject<void>();

  @Input() ad!: Ad;
  @Input() isMyAd: boolean = false;

  private _blockedIntervals: { start: Date, end: Date }[] = [];

  @Input() set blockedIntervals(value: { start: Date, end: Date }[]) {
    this._blockedIntervals = value || [];
    this.generateCalendar();
  }

  // Calendar state
  displayDate: Date = new Date();
  daysInMonth: CalendarDay[] = [];
  weekdays: string[] = ['Pon', 'Uto', 'Sre', 'Čet', 'Pet', 'Sub', 'Ned'];

  startDate: Date | null = null;
  endDate: Date | null = null;
  numberOfDays: number = 0;
  totalPrice: number = 0;
  calendarHeight: number = 0;
  isSendingRequest = false;
  isBlockingDates = false;

  constructor(
    private contractService: ContractService,
    private toastService: ToastService,
    private adService: AdService,
    private router: Router,
    private datePipe: DatePipe,
    private authService: AuthService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ad'] && this.ad) {
      this.generateCalendar();
    }
  }

  generateCalendar(): void {
    const year = this.displayDate.getFullYear();
    const month = this.displayDate.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    const days: CalendarDay[] = [];

    let startDayOfWeek = firstDayOfMonth.getDay();
    startDayOfWeek = (startDayOfWeek === 0) ? 6 : startDayOfWeek - 1;

    for (let i = 0; i < startDayOfWeek; i++) {
      days.push({} as CalendarDay);
    }

    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
      const date = new Date(year, month, day);
      days.push(this.createCalendarDay(date));
    }

    while (days.length % 7 !== 0) {
      days.push({} as CalendarDay);
    }

    this.daysInMonth = days;
    this.calendarHeight = (days.length / 7) * 44;
  }

  createCalendarDay(date: Date): CalendarDay {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return {
      date: date,
      dayOfMonth: date.getDate(),
      isCurrentMonth: true,
      isPast: date < today,
      isBlocked: this.isDateBlocked(date),
      isSelected: this.isDateSelected(date),
      isStartDate: this.isSameDay(date, this.startDate),
      isEndDate: this.isSameDay(date, this.endDate),
      isInRange: this.isInRange(date)
    };
  }

  selectDate(day: CalendarDay | null): void {
    if (!day || day.isPast || day.isBlocked) {
      return;
    }
    if (!this.startDate || (this.startDate && this.endDate)) {
      this.startDate = day.date;
      this.endDate = null;
    } else if (day.date < this.startDate) {
      this.startDate = day.date;
    } else {
      this.endDate = day.date;
      if (this.isRangeBlocked(this.startDate, this.endDate)) {
        this.clearDates();
        return;
      }
    }
    this.recalculatePrice();
    this.generateCalendar();
  }

  clearDates(): void {
    this.startDate = null;
    this.endDate = null;
    this.recalculatePrice();
    this.generateCalendar();
  }

  changeMonth(amount: number): void {
    const currentMonth = this.displayDate.getMonth();
    const currentYear = this.displayDate.getFullYear();
    this.displayDate = new Date(currentYear, currentMonth + amount, 1);
    this.generateCalendar();
  }

  recalculatePrice(): void {
    if (this.startDate && this.endDate && this.ad) {
      const diffTime = Math.abs(this.endDate.getTime() - this.startDate.getTime());
      this.numberOfDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      this.totalPrice = this.calculateTieredPrice(this.numberOfDays);
    } else {
      this.numberOfDays = 0;
      this.totalPrice = 0;
    }
  }

  private calculateTieredPrice(days: number): number {
    return this.buildBreakdown(days).total;
  }

  /**
   * Vraca kompleksnu razbijenu cenu za prikaz breakdown-a.
   * Primer: "1 nedelja x 50 = 50 + 2 dana x 10 = 20 → ukupno 70"
   */
  buildBreakdown(days: number): { total: number; parts: { label: string; unitPrice: number; qty: number; sub: number }[] } {
    if (!this.ad || days <= 0) return { total: 0, parts: [] };
    const parts: { label: string; unitPrice: number; qty: number; sub: number }[] = [];
    let total = 0;
    let remaining = days;
    const interval = this.ad.priceInterval;

    if (interval === 'PER_MONTH') {
      if (this.ad.pricePerMonth) {
        const wholeMonths = Math.floor(remaining / 30);
        const rem = remaining % 30;
        if (wholeMonths > 0) {
          const sub = wholeMonths * this.ad.pricePerMonth;
          parts.push({ label: this.plural(wholeMonths, 'mesec', 'meseca', 'meseci'), unitPrice: this.ad.pricePerMonth, qty: wholeMonths, sub });
          total += sub;
        }
        if (rem > 0) {
          const dailyEquiv = Math.round(this.ad.pricePerMonth / 30);
          const sub = rem * dailyEquiv;
          parts.push({ label: this.plural(rem, 'dan', 'dana', 'dana'), unitPrice: dailyEquiv, qty: rem, sub });
          total += sub;
        }
        return { total, parts };
      }
      const sub = remaining * this.ad.price;
      parts.push({ label: this.plural(remaining, 'dan', 'dana', 'dana'), unitPrice: this.ad.price, qty: remaining, sub });
      return { total: sub, parts };
    }

    // PER_DAY (default za većinu oglasa)
    if (this.ad.pricePerMonth && remaining >= 30) {
      const months = Math.floor(remaining / 30);
      const sub = months * this.ad.pricePerMonth;
      parts.push({ label: this.plural(months, 'mesec', 'meseca', 'meseci'), unitPrice: this.ad.pricePerMonth, qty: months, sub });
      total += sub;
      remaining -= months * 30;
    }
    if (this.ad.pricePerWeek && remaining >= 7) {
      const weeks = Math.floor(remaining / 7);
      const sub = weeks * this.ad.pricePerWeek;
      parts.push({ label: this.plural(weeks, 'nedelja', 'nedelje', 'nedelja'), unitPrice: this.ad.pricePerWeek, qty: weeks, sub });
      total += sub;
      remaining -= weeks * 7;
    }
    if (remaining > 0) {
      const sub = remaining * this.ad.price;
      parts.push({ label: this.plural(remaining, 'dan', 'dana', 'dana'), unitPrice: this.ad.price, qty: remaining, sub });
      total += sub;
    }
    return { total, parts };
  }

  get priceBreakdown() { return this.buildBreakdown(this.numberOfDays); }

  get currencySymbol(): string {
    if (!this.ad) return 'din';
    return this.ad.currency === 'EUR' ? '€' : 'din';
  }

  private plural(n: number, one: string, few: string, many: string): string {
    const mod10 = n % 10;
    const mod100 = n % 100;
    if (mod10 === 1 && mod100 !== 11) return `${n} ${one}`;
    if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return `${n} ${few}`;
    return `${n} ${many}`;
  }

  isDateBlocked(date: Date): boolean {
    const d = new Date(date).setHours(0, 0, 0, 0);
    return this._blockedIntervals.some(interval => {
      const s = new Date(interval.start).setHours(0, 0, 0, 0);
      const e = new Date(interval.end).setHours(0, 0, 0, 0);
      return d >= s && d <= e;
    });
  }

  isDateSelected(date: Date): boolean {
    if (!this.startDate || !this.endDate) return false;
    return date > this.startDate && date < this.endDate;
  }

  isRangeBlocked(start: Date, end: Date): boolean {
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      if (this.isDateBlocked(new Date(d))) return true;
    }
    return false;
  }

  isSameDay(date1: Date | null, date2: Date | null): boolean {
    if (!date1 || !date2) return false;
    return date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate();
  }

  isInRange(date: Date): boolean {
    if (!this.startDate || !this.endDate) return false;
    const day = new Date(date).setHours(0, 0, 0, 0);
    const start = new Date(this.startDate).setHours(0, 0, 0, 0);
    const end = new Date(this.endDate).setHours(0, 0, 0, 0);
    return day > start && day < end;
  }

  sendRequest(): void {
    if (this.isSendingRequest || !this.startDate || !this.endDate || !this.ad) return;

    if (!this.authService.currentUserValue) {
      this.router.navigate(['/login']);
      return;
    }

    this.isSendingRequest = true;
    const request: CreateRentalContractRequest = {
      adId: this.ad.id,
      startDate: this.datePipe.transform(this.startDate, 'yyyy-MM-dd')!,
      endDate: this.datePipe.transform(this.endDate, 'yyyy-MM-dd')!,
      agreedPrice: this.totalPrice,
      amount: 1,
      currency: this.ad.currency
    };

    this.contractService.createRentalContract(request).subscribe({
      next: () => {
        this.isSendingRequest = false;
        this.toastService.showSuccess('Uspesno ste poslali zahtev!');
      },
      error: () => {
        this.isSendingRequest = false;
        this.toastService.showError('Zahtev nije poslat!');
      }
    });
  }

  blockDates(): void {
    if (this.isBlockingDates || !this.startDate || !this.endDate || !this.ad) return;

    this.isBlockingDates = true;
    const request: CreateRentalContractRequest = {
      adId: this.ad.id,
      startDate: this.datePipe.transform(this.startDate, 'yyyy-MM-dd')!,
      endDate: this.datePipe.transform(this.endDate, 'yyyy-MM-dd')!,
      agreedPrice: 0,
      amount: this.ad.totalQuantity,
      currency: this.ad.currency
    };

    this.contractService.blockDates(request).pipe(
      switchMap(() => this.adService.getAdById(this.ad.id)),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (updatedAd) => {
        this.isBlockingDates = false;
        this._blockedIntervals = (updatedAd.blockedIntervals || []).map(interval => ({
          start: new Date(interval.from),
          end: new Date(interval.to),
        }));
        this.clearDates();
        this.generateCalendar();
        this.toastService.showSuccess('Datumi su uspešno blokirani!');
      },
      error: () => {
        this.isBlockingDates = false;
        this.toastService.showError('Greška pri blokiranju datuma.');
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
