import {Component, OnInit} from '@angular/core';
import {AdCardComponent} from '../../components/ad-card/ad-card.component';
import {CommonModule} from '@angular/common';
import {AdPreview, Page} from '../../../../shared/models/adPreview';
import {Observable} from 'rxjs';

import {AdService} from '../../services/ad.service';



@Component({
  selector: 'app-ad-list',
  standalone: true,
  imports: [CommonModule, AdCardComponent],
  templateUrl: './ad-list.component.html',
  styleUrl: './ad-list.component.css'
})
export class AdListComponent implements OnInit {

    adsPage$!:  Observable<Page<AdPreview>>
    constructor(private adService: AdService) {
    }
  ngOnInit(): void {
    this.adsPage$ = this.adService.search({ page: 0,
      size: 10});
    this.adsPage$.subscribe(res => {
      console.log('ADS PAGE:', res);
    });
  }
}
