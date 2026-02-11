import {Component, OnInit} from '@angular/core';
import {Ad} from '../../../../shared/models/ad.model';
import {CommonModule, DatePipe} from '@angular/common';
import {Observable, switchMap, tap} from 'rxjs';
import {AdService} from '../../services/ad.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {CalendarDay} from '../../../../shared/models/day.model';
import {ContractService} from '../../../contracts/services/contract.service';
import {CreateRentalContractRequest} from '../../../../shared/models/create-rental-contract-request';

@Component({
  selector: 'app-ad-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ad-details.component.html',
  styleUrl: './ad-details.component.css'
})
export class AdDetailsComponent implements OnInit {
  ad$!: Observable<Ad>;
  selectedImageUrl: string | null = null;
  currentDate: Date = new Date();
  displayDate: Date = new Date();
  daysInMonth: CalendarDay[] = [];
  months: string[] = ["January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"];
  weekdays: string[] = ['Pon', 'Uto', 'Sre', 'Čet', 'Pet', 'Sub', 'Ned'];

  startDate: Date | null = null;
  endDate: Date | null = null;
  numberOfDays: number = 0;
  totalPrice: number = 0;
  currentAd!: Ad;


  private blockedIntervals: { start: Date, end: Date }[] = [];

  constructor(private adService: AdService,
              private route: ActivatedRoute,
              private contractService: ContractService,
              private router: Router,
              private datePipe: DatePipe,) {
  }

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
        this.blockedIntervals = (ad.blockedIntervals || []).map(interval => ({
          start: new Date(interval.from),
          end: new Date(interval.to),
        }));
        this.generateCalendar();

      })
    )
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
    this.daysInMonth = days;
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
    }
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
    this.displayDate.setMonth(this.displayDate.getMonth() + amount);
    this.generateCalendar();
  }

  recalculatePrice(): void {
    if (this.startDate && this.endDate) {
      const diffTime = Math.abs(this.endDate.getTime() - this.startDate.getTime());
      console.log("recalculatePrice", diffTime);
      this.numberOfDays = Math.ceil(diffTime / (1000 * 60 * 60 * 25)) + 1
      this.totalPrice = this.numberOfDays * this.currentAd.price;
    } else {
      this.numberOfDays = 0;
      this.totalPrice = 0;
    }
  }

  isDateBlocked(date: Date): boolean {
    return this.blockedIntervals.some(interval => date >= interval.start && date <= interval.end);
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

  selectImage(imageUrl: string) {
    this.selectedImageUrl = imageUrl;
  }

  handleImageError(event: any) {
    event.target.src = 'assets/images/placeholder.png';
  }

  sendRequest(){
    if(!this.startDate || !this.endDate || !this.ad$) {
      return;
    }
    if (!this.currentAd) return;

    const request: CreateRentalContractRequest = {
      adId: this.currentAd.id,
      startDate: this.datePipe.transform(this.startDate, 'yyyy-MM-dd')!,
      endDate: this.datePipe.transform(this.endDate, 'yyyy-MM-dd')!,
      agreedPrice: this.totalPrice,
      amount: 1
    };
    this.contractService.createRentalContract(request).subscribe({
      next: (response) => {
        alert('Zahtev uspesno poslat');

    },
      error: (err) => {
        console.error(err);
        alert('Došlo je do greške prilikom slanja zahteva.');
      }
      }
    )
  }

}
