import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule, NgForOf} from "@angular/common";
import {ReviewFormComponent} from '../../components/review-form/review-form.component';
import {ReviewService} from '../../services/review.service';
import {UserService} from '../../../user/services/user.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ReviewCardComponent} from '../../components/review-card/review-card.component';

import {Review} from '../../../../shared/models/review';
import {UserShort} from '../../../../shared/models/userShort';
import {InitialsPipe} from '../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-review',
  imports: [
    CommonModule,
    NgForOf,
    ReviewFormComponent,
    ReviewCardComponent,
    InitialsPipe
  ],
  templateUrl: './review.component.html',
  styleUrl: './review.component.css'
})

export class ReviewComponent implements OnInit {

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

  reviews: any[] = [];

  constructor(private userService: UserService,
              private reviewService: ReviewService,
              private route: ActivatedRoute,
              private router: Router) {}

  ngOnInit() {
    // ISPRAVKA 1: ID korisnika je u samoj putanji (/user/5/reviews),
    // Zato koristimo paramMap (bez 'query')
    this.targetUserId = Number(this.route.snapshot.paramMap.get('id'));

    // ISPRAVKA 2: ID ugovora je posle upitnika (?contractId=12),
    // Zato koristimo queryParamMap
    const contractParam = this.route.snapshot.queryParamMap.get('contractId');

    if(contractParam) {
      this.contractIdToReview = Number(contractParam);
    }
    this.loadReviews();

    this.userService.getUserProfile(this.targetUserId).subscribe(userData => {
      this.user = userData;
    })

  }

  private loadReviews() {
    this.reviewService.getReviewsForUser(this.targetUserId).subscribe(reviews => {
      this.reviews = reviews.content;
      this.applyFilter();
    });
  }

  onReviewSubmitted() {
    this.toggleForm() // Sakrij formu
    this.contractIdToReview = null;
    this.loadReviews(); // Osveži listu da se pojavi nova ocena
    this.userService.getUserProfile(this.targetUserId).subscribe(userData => {
      this.user = userData;
    });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {contractId: null},
      queryParamsHandling: 'merge'
    })
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
