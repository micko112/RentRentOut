import {Component, Input} from '@angular/core';
import {Review} from '../../../../shared/models/review';
import {DatePipe, NgClass, NgIf} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-review-card',
  imports: [
    NgClass,
    DatePipe,
    NgIf,
    RouterLink
  ],
  standalone: true,
  templateUrl: './review-card.component.html',
  styleUrl: './review-card.component.css'
})

export class ReviewCardComponent {
  @Input() public review!: Review;

  optionMap: { [key: string]: string } = {
    'YES': 'Da',
    'NO': 'Ne',
    'COULD_BE_BETTER': 'Može bolje'
  };

}
