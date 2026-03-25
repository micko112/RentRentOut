package org.landm.service.impl;

import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.dto.review.ReviewEligibilityDto;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.landm.entity.RentalContract;
import org.landm.entity.Review;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.mapper.ReviewMapper;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.ReviewRepository;
import org.landm.repository.UserRepository;
import org.landm.entity.Enums.NotificationType;
import org.landm.service.NotificationPersistenceService;
import org.landm.service.ReviewService;
import org.landm.util.HtmlSanitizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RentalContractRepository rentalContractRepository;
    private final NotificationPersistenceService notifPersistenceService;

    public ReviewServiceImpl(ReviewMapper reviewMapper, UserRepository userRepository, ReviewRepository reviewRepository,
                             RentalContractRepository rentalContractRepository, NotificationPersistenceService notifPersistenceService) {
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.rentalContractRepository = rentalContractRepository;
        this.notifPersistenceService = notifPersistenceService;
    }

    @Override
    public ReviewType calculateReviewType(ReviewOption payment, ReviewOption communication, ReviewOption agreement) {

        int noCount =0;
        int couldBetterCount =0;

        if(payment == ReviewOption.NO) noCount++;
        if(communication == ReviewOption.NO) noCount++;
        if(agreement == ReviewOption.NO) noCount++;

        if(payment == ReviewOption.COULD_BE_BETTER) couldBetterCount++;
        if(communication == ReviewOption.COULD_BE_BETTER) couldBetterCount++;
        if(agreement == ReviewOption.COULD_BE_BETTER) couldBetterCount++;

        if(noCount == 3 || (noCount == 2 && couldBetterCount ==1 ) || (couldBetterCount ==2 && noCount ==1) ){
            return ReviewType.NEGATIVE;
        }
        return ReviewType.POSITIVE;
    }

    @Override
    @Transactional
    public ReviewDto createReview(CreateReviewRequestDto dto, Long reviewerId) {


        ReviewType type = calculateReviewType(dto.getPaymentOk(), dto.getCommunicationOk(), dto.getAgreementOk());

        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> new UserNotFoundException("User not found"));

        RentalContract rc = rentalContractRepository.findById(dto.getContractId()).orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if(reviewRepository.existsByContractIdAndReviewerId(rc.getId(), reviewerId)){
            throw new IllegalStateException("Već ste ostavili ocenu za ovaj ugovor.");
        }

        if (rc.getContractStatus() != ContractStatus.FINISHED
                && rc.getContractStatus() != ContractStatus.CANCELLED_AFTER_ACCEPT) {
            throw new IllegalStateException("Ugovor nije završen ili otkazan nakon prihvatanja.");
        }

        if (rc.getEndDate().isBefore(LocalDate.now().minusDays(30))) {
            throw new IllegalStateException("Rok za ocenjivanje (30 dana) je istekao.");
        }

        User reviewee;
        if(rc.getLessee().getId().equals(reviewerId)){
            reviewee = rc.getAd().getOwner();
        }else if(rc.getAd().getOwner().getId().equals(reviewerId)){
            reviewee = rc.getLessee();
        }else throw new AccessDeniedException("Niste učesnik u ovom ugovoru.");

        if(reviewerId.equals(reviewee.getId())){
            throw new IllegalArgumentException("Ne možete sami sebi ostaviti ocenu.");
        }

        Review review = new Review(
                rc,
                reviewer,
                reviewee,
                dto.getPaymentOk(),
                dto.getCommunicationOk(),
                dto.getAgreementOk(),
                type,
                HtmlSanitizer.sanitize(dto.getComment()),
                LocalDateTime.now()
        );

        if(type == ReviewType.POSITIVE){
            reviewee.setPositiveReviews(reviewee.getPositiveReviews() + 1);
        }else {
            reviewee.setNegativeReviews(reviewee.getNegativeReviews() + 1);
        }
        try {
            reviewRepository.save(review);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException("Već ste ostavili ocenu za ovaj ugovor.");
        }
        userRepository.save(reviewee);
        String reviewerName = reviewer.getFirstname() + " " + reviewer.getLastname();
        String sentiment = review.getReviewType() == ReviewType.POSITIVE ? "pozitivnu" : "negativnu";
        notifPersistenceService.create(
            reviewee.getId(), NotificationType.NEW_REVIEW,
            "Nova ocena",
            reviewerName + " vam je ostavio/la " + sentiment + " ocenu.",
            review.getId(), "REVIEW", reviewerName
        );

        return reviewMapper.toDto(review);
    }

    @Override
    public ReviewEligibilityDto checkEligibility(Long contractId, Long reviewerId) {
        RentalContract rc = rentalContractRepository.findById(contractId).orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        boolean isParty = rc.getLessee().getId().equals(reviewerId)
                || rc.getAd().getOwner().getId().equals(reviewerId);
        if (!isParty) {
            return new ReviewEligibilityDto(false, "Niste učesnik u ovom ugovoru.");
        }

        if (rc.getContractStatus() != ContractStatus.FINISHED
                && rc.getContractStatus() != ContractStatus.CANCELLED_AFTER_ACCEPT) {
            return new ReviewEligibilityDto(false, "Ne može se utvrditi da je do iznajmljivanja došlo.");
        }
        if (rc.getEndDate().isBefore(LocalDate.now().minusDays(30))) {
            return new ReviewEligibilityDto(false, "Ovaj dogovor je završen pre više od 30 dana.");
        }
        if (reviewRepository.existsByContractIdAndReviewerId(contractId, reviewerId)) {
            return new ReviewEligibilityDto(false, "Već ste ocenili korisnika za ovu saradnju.");
        }
        return new ReviewEligibilityDto(true, "");
    }

    @Override
    public Page<ReviewDto> getAllForUser(Pageable pageable, Long revieweeId) {
        Page<Review> page = reviewRepository.findAllByRevieweeId(revieweeId, pageable);
        return page.map(reviewMapper::toDto);

    }

}
