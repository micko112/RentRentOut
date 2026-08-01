import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import {Review} from '../../../../shared/models/review';
import {DatePipe, NgClass, NgIf} from '@angular/common';
import {RouterLink} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';

@Component({
  selector: 'app-review-card',
  imports: [
    NgClass,
    DatePipe,
    NgIf,
    RouterLink,
    TranslateModule
  ],
  standalone: true,
  templateUrl: './review-card.component.html',
  styleUrl: './review-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewCardComponent {
  @Input() public review!: Review;

  optionMap: { [key: string]: string } = {
    'YES': 'reviewCard.answer_yes',
    'NO': 'reviewCard.answer_no',
    'COULD_BE_BETTER': 'reviewCard.answer_could_be_better'
  };

}
