import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SidebarStateService {
  private collapsedSubject = new BehaviorSubject<boolean>(
    typeof window !== 'undefined' ? window.innerWidth < 1600 : false
  );
  collapsed$ = this.collapsedSubject.asObservable();

  get isCollapsed(): boolean {
    return this.collapsedSubject.value;
  }

  toggle(): void {
    this.collapsedSubject.next(!this.collapsedSubject.value);
  }

  setCollapsed(value: boolean): void {
    this.collapsedSubject.next(value);
  }
}
