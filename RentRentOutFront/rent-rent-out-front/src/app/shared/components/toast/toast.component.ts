import { Component } from '@angular/core';
import {CommonModule, NgForOf} from '@angular/common';
import {ToastService} from '../../services/toast.service';


@Component({
  selector: 'app-toast',
  imports: [
    NgForOf,
    CommonModule
  ],
  standalone: true,
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent {

  constructor(public toastService: ToastService) {

  }
}
