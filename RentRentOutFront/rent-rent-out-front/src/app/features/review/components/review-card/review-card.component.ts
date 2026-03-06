import {Component, Input} from '@angular/core';
import {Review} from '../../../../shared/models/review';
import {DatePipe, NgClass, NgIf} from '@angular/common';

@Component({
  selector: 'app-review-card',
  imports: [
    NgClass,
    DatePipe,
    NgIf
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

  // MAPA 2: Prevod za glavni tip ocene (Ovo je opciono, ali korisno)
  typeMap: { [key: string]: string } = {
    'POSITIVE': 'Pozitivna',
    'NEGATIVE': 'Negativna'
  };

}
