package org.landm.controller;


import jakarta.validation.Valid;
import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    //@PathVariable služi za čitanje varijabli direktno iz URL-a (npr. /api/review/{id}). On ne može da čita ceo JSON objekat
    //@RequestBody služi za čitanje JSON tela (body) HTTP zahteva i njegovo pretvaranje u Java objekat (DTO).
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewRequestDto req,
                                                  Authentication auth){
        // onaj ko kreira ocenu
        long reviewerId = Long.parseLong(auth.getName());

        return new ResponseEntity<>(reviewService.createReview(req, reviewerId), HttpStatus.CREATED);
    }
}
