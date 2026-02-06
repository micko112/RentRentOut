import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterModule} from '@angular/router';
import {AdPreview} from '../../../../shared/models/adPreview.model';
import {AdService} from '../../services/ad.service';

@Component({
  selector: 'app-ad-card',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterModule],
  templateUrl: './ad-card.component.html',
  styleUrl: './ad-card.component.css'
})
export class AdCardComponent {
  @Input() ad!: AdPreview;
  @Input() viewMode: 'grid' | 'list' = 'grid';

  get imageUrl(): string {
    if (this.ad.thumbnail) {
      return this.ad.thumbnail;
    }
    return 'assets/images/placeholder.png';
  }

  handleImageError(event: any) {
    event.target.src = 'assets/images/placeholder.png';
    event.target.onerror = null;
  }
  intervalMap: {[key: string]: string} = {
    'PER_DAY': '/dan',
    'PER_HOUR': '/sat',
    'PER_MONTH': '/mesečno'
  };


}
