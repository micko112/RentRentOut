import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
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
export class RentalCalendarComponent implements OnChanges {

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
    if (!this.ad) return 0;
    const interval = this.ad.priceInterval;
    if (interval === 'PER_MONTH' || interval === 'MONTHLY') {
      const months = days / 30;
      if (this.ad.pricePerMonth && months >= 1) {
        return Math.round(months * this.ad.pricePerMonth);
      }
    }
    if (interval === 'PER_DAY' || interval === 'DAILY') {
      if (this.ad.pricePerMonth && days >= 30) {
        const months = Math.floor(days / 30);
        const remainingDays = days % 30;
        if (this.ad.pricePerWeek && remainingDays >= 7) {
          const weeks = Math.floor(remainingDays / 7);
          const leftoverDays = remainingDays % 7;
          return months * this.ad.pricePerMonth + weeks * this.ad.pricePerWeek + leftoverDays * this.ad.price;
        }
        return months * this.ad.pricePerMonth + remainingDays * this.ad.price;
      }
      if (this.ad.pricePerWeek && days >= 7) {
        const weeks = Math.floor(days / 7);
        const remainingDays = days % 7;
        return weeks * this.ad.pricePerWeek + remainingDays * this.ad.price;
      }
    }
    return days * this.ad.price;
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
    if (!this.startDate || !this.endDate || !this.ad) return;

    if (!this.authService.currentUserValue) {
      this.router.navigate(['/login']);
      return;
    }

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
        this.toastService.showSuccess('Uspesno ste poslali zahtev!');
      },
      error: () => {
        this.toastService.showError('Zahtev nije poslat!');
      }
    });
  }

  blockDates(): void {
    if (!this.startDate || !this.endDate || !this.ad) return;

    const request: CreateRentalContractRequest = {
      adId: this.ad.id,
      startDate: this.datePipe.transform(this.startDate, 'yyyy-MM-dd')!,
      endDate: this.datePipe.transform(this.endDate, 'yyyy-MM-dd')!,
      agreedPrice: 0,
      amount: this.ad.totalQuantity,
      currency: this.ad.currency
    };

    this.contractService.blockDates(request).subscribe({
      next: () => {
        this.toastService.showSuccess('Datumi su uspesno blokirani!');
        this.clearDates();

        this.adService.getAdById(this.ad.id).subscribe({
          next: (updatedAd) => {
            this._blockedIntervals = (updatedAd.blockedIntervals || []).map(interval => ({
              start: new Date(interval.from),
              end: new Date(interval.to),
            }));
            this.generateCalendar();
          },
          error: () => {}
        });
      },
      error: () => {
        this.toastService.showError('Greška pri blokiranju datuma.');
      }
    });
  }
}
