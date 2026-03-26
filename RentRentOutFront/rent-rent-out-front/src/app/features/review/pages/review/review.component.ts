import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {ReviewFormComponent} from '../../components/review-form/review-form.component';
import {ReviewService} from '../../services/review.service';
import {UserService} from '../../../user/services/user.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ReviewCardComponent} from '../../components/review-card/review-card.component';
import {Subject, switchMap} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

import {Review} from '../../../../shared/models/review';
import {UserShort} from '../../../../shared/models/userShort';
import {InitialsPipe} from '../../../../shared/pipes/initials.pipe';
import {AuthService} from '../../../auth/services/auth.service';
import {ContractService} from '../../../contracts/services/contract.service';
import {RentalContract} from '../../../../shared/models/rental-contract.model';

@Component({
  selector: 'app-review',
  imports: [
    CommonModule,
    ReviewFormComponent,
    ReviewCardComponent,
    InitialsPipe,
    DatePipe
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

  showForm = false;
  showContractSelect = false;

  user: UserShort | null = null;
  isOwnProfile = false;

  filteredReviews: Review[] = [];
  reviews: Review[] = [];

  targetUserId!: number;
  contractIdToReview: number | null = null;
  finishedContracts: RentalContract[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private userService: UserService,
    private reviewService: ReviewService,
    private authService: AuthService,
    private contractService: ContractService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // Reaguje na svaku promenu :id parametra (fix za route reuse)
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.targetUserId = Number(params.get('id'));
      this.isOwnProfile = this.authService.currentUserValue?.id === this.targetUserId;
      this.showForm = false;
      this.showContractSelect = false;
      this.contractIdToReview = null;

      const contractParam = this.route.snapshot.queryParamMap.get('contractId');
      if (contractParam) {
        this.contractIdToReview = Number(contractParam);
        this.showForm = true;
      }

      this.loadData();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadData() {
    this.userService.getUserProfile(this.targetUserId).pipe(takeUntil(this.destroy$)).subscribe({
      next: userData => { this.user = userData; },
      error: () => {}
    });

    this.loadReviews();

    if (!this.isOwnProfile && this.authService.currentUserValue) {
      this.contractService.getFinishedWithUser(this.targetUserId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: contracts => { this.finishedContracts = contracts; },
          error: () => {}
        });
    }
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

  onOceniClick(): void {
    if (this.finishedContracts.length === 0) {
      // Nema završenih ugovora — otvori formu bez contractId
      this.showForm = true;
    } else {
      // Ima ugovora — prikaži select
      this.showContractSelect = true;
    }
  }

  onContractSelected(contractId: number): void {
    this.contractIdToReview = contractId;
    this.showContractSelect = false;
    this.showForm = true;
  }

  onReviewSubmitted() {
    this.showForm = false;
    this.showContractSelect = false;
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

  contractLabel(c: RentalContract): string {
    return `${c.adDto?.title ?? 'Oglas'} · ${c.startDate} – ${c.endDate}`;
  }
}
