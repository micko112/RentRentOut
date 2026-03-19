import {Component, OnInit} from '@angular/core';

import {UserService} from '../../services/user.service';
import {ActivatedRoute, Router} from '@angular/router';
import {AdService} from '../../../ads/services/ad.service';

import {Observable, tap} from 'rxjs';

import {ReviewService} from '../../../review/services/review.service';
import {ReviewCardComponent} from '../../../review/components/review-card/review-card.component';
import {AdCardComponent} from '../../../ads/components/ad-card/ad-card.component';
import {AsyncPipe, DatePipe, JsonPipe, NgForOf, NgIf} from '@angular/common';
import {PublicProfile} from '../../../../shared/models/public-profile';
import {InitialsPipe} from '../../../../shared/pipes/initials.pipe';
import {ReviewFormComponent} from '../../../review/components/review-form/review-form.component';
import {Review} from '../../../../shared/models/review';

@Component({
  selector: 'app-user-profile',
  imports: [
    ReviewCardComponent,
    AdCardComponent,
    NgForOf,
    NgIf,
    AsyncPipe,
    DatePipe,
    InitialsPipe,
    JsonPipe,
    ReviewFormComponent
  ],
  templateUrl: './user-profile.component.html',
  standalone: true,
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {

  constructor(private userService: UserService,
              private adService: AdService,
              private route: ActivatedRoute,
              private router: Router,
              private reviewService: ReviewService,) {
  }
  userId!: number;

  profile$!: Observable<PublicProfile>;

  totalAds: number = 0;
  totalReviews: number = 0;

  reviews: Review[] = [];
  filteredReviews: Review[] = [];
  activeReviewTab: 'ALL' | 'LESSOR' | 'LESSEE' = 'ALL';
  activeTypeTab: 'ALL' | 'POSITIVE' | 'NEGATIVE' = 'ALL';

  setReviewTab(tab: 'ALL' | 'LESSOR' | 'LESSEE') {
    this.activeReviewTab = tab;
    this.applyFilter();
  }

  setTypeTab(tab: 'ALL' | 'POSITIVE' | 'NEGATIVE') {
    this.activeTypeTab = tab;
    this.applyFilter();
  }
  showForm: boolean = false;

  activeTab: "ads" | "reviews" = 'ads';

  setTab(tab: "ads" | "reviews") {
    this.activeTab = tab;

  }
  ngOnInit() {
    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    this.profile$ = this.userService.getPublicProfile(this.userId).pipe(
      tap(data => {
        this.reviews = data.reviews.content;
        this.applyFilter();
      })
    );


  }

  applyFilter(): void {
    this.filteredReviews = this.reviews.filter(review => {
      const roleMatch = this.activeReviewTab === 'ALL' || review.revieweeRole === this.activeReviewTab;
      const typeMatch = this.activeTypeTab === 'ALL' || review.reviewType === this.activeTypeTab;
      return roleMatch && typeMatch;
    });
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }
}
