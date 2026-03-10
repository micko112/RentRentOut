import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';



export interface ToastMessage {
    id: number;
    text: string;
    type: 'success' | 'warning' | 'error';
}
@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts: ToastMessage[] = [];

  private toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  public toasts$ = this.toastsSubject.asObservable();

  private idCounter = 0;

  showSucces(text: string) {
    this.addToast(text, 'success')
  }
  showError(text: string) {
    this.addToast(text, 'error');
  }
  showWarning(text: string) {
    this.addToast(text, 'warning');
  }
  addToast(text: string, type: 'success' | 'error' | 'warning') {
    const id = this.idCounter++;
    const newToast: ToastMessage = {id, text, type};

    this.toasts.push(newToast);
    this.toastsSubject.next(this.toasts);

    setTimeout(() => {
      this.removeToast(id);
    }, 3500);

  }

  removeToast(id: number) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.toastsSubject.next(this.toasts);

  }

}
