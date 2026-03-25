import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReviewFormComponent} from '../../components/review-form/review-form.component';
import {ReviewService} from '../../services/review.service';
import {UserService} from '../../../user/services/user.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ReviewCardComponent} from '../../components/review-card/review-card.component';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

import {Review} from '../../../../shared/models/review';
import {UserShort} from '../../../../shared/models/userShort';
import {InitialsPipe} from '../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-review',
  imports: [
    CommonModule,
    ReviewFormComponent,
    ReviewCardComponent,
    InitialsPipe
  ],
  templateUrl: './review.component.html',
  styleUrl: './review.component.css'
})

export class ReviewComponent implements OnInit, OnDestroy {

  activeTab: 'ALL' | 'LESSOR' | 'LESSEE' = 'ALL';
  activeTypeTab: 'ALL' | 'POSITIVE' | 'NEGATIVE' = 'ALL';

  setTab(tab: 'ALL' | 'LESSOR' | 'LESSEE') {
    this.activeTab = tab;
    this.applyFilter();
  }

  setTypeTab(tab: 'ALL' | 'POSITIVE' | 'NEGATIVE') {
    this.activeTypeTab = tab;
    this.applyFilter();
  }

  showForm: boolean = false;

  user: UserShort | null = null;

  filteredReviews: Review[] = [];

  targetUserId!: number;
  contractIdToReview: number | null = null;

  reviews: Review[] = [];

  private destroy$ = new Subject<void>();

  constructor(private userService: UserService,
              private reviewService: ReviewService,
              private route: ActivatedRoute,
              private router: Router) {}

  ngOnInit() {
    this.targetUserId = Number(this.route.snapshot.paramMap.get('id'));

    const contractParam = this.route.snapshot.queryParamMap.get('contractId');

    if(contractParam) {
      this.contractIdToReview = Number(contractParam);
    }
    this.loadReviews();

    this.userService.getUserProfile(this.targetUserId).pipe(takeUntil(this.destroy$)).subscribe({
      next: userData => { this.user = userData; },
      error: () => {}
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadReviews() {
    this.reviewService.getReviewsForUser(this.targetUserId).pipe(takeUntil(this.destroy$)).subscribe({
      next: reviews => {
        this.reviews = reviews.content;
        this.applyFilter();
      },
      error: () => {}
    });
  }

  onReviewSubmitted() {
    this.toggleForm();
    this.contractIdToReview = null;
    this.loadReviews();
    this.userService.getUserProfile(this.targetUserId).pipe(takeUntil(this.destroy$)).subscribe({
      next: userData => { this.user = userData; },
      error: () => {}
    });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {contractId: null},
      queryParamsHandling: 'merge'
    });
  }

  applyFilter(): void {
    this.filteredReviews = this.reviews.filter(review => {
      const roleMatch = this.activeTab === 'ALL' || review.revieweeRole === this.activeTab;
      const typeMatch = this.activeTypeTab === 'ALL' || review.reviewType === this.activeTypeTab;
      return roleMatch && typeMatch;
    });
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }
}
