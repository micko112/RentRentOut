export interface CalendarDay {
  date: Date;
  dayOfMonth: number;
  isCurrentMonth: boolean;
  isPast: boolean;
  isBlocked: boolean;
  isSelected: boolean;
  isStartDate: boolean;
  isEndDate: boolean;
  isInRange: boolean;
}
