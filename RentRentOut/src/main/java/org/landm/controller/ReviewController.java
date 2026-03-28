package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.dto.review.ReviewEligibilityDto;
import org.landm.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("reviews")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewRequestDto req,
                                                  Authentication auth){
        Long reviewerId = Long.parseLong(auth.getName());

        return new ResponseEntity<>(reviewService.createReview(req, reviewerId), HttpStatus.CREATED);
    }
    @GetMapping("user/{revieweeId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(Pageable pageable, @PathVariable Long revieweeId){

        Page<ReviewDto> reviewDtoPage = reviewService.getAllForUser(pageable, revieweeId);
        return ResponseEntity.ok(reviewDtoPage);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("reviews/check/{contractId}")
    public ResponseEntity<ReviewEligibilityDto> checkReview(@PathVariable Long contractId, Authentication auth){
        Long reviewerId = Long.parseLong(auth.getName());

        return ResponseEntity.ok(reviewService.checkEligibility(contractId, reviewerId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("reviews/contract-with/{userId}")
    public ResponseEntity<?> findContractWith(@PathVariable Long userId, Authentication auth) {
        Long currentUserId = Long.parseLong(auth.getName());
        Long contractId = reviewService.findContractWithUser(currentUserId, userId);
        return ResponseEntity.ok(Collections.singletonMap("contractId", contractId));
    }

}
