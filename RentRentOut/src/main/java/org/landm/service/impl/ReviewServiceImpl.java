package org.landm.service.impl;

import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.landm.entity.RentalContract;
import org.landm.entity.Review;
import org.landm.entity.User;
import org.landm.mapper.ReviewMapper;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.ReviewRepository;
import org.landm.repository.UserRepository;
import org.landm.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RentalContractRepository rentalContractRepository;
    public ReviewServiceImpl(ReviewMapper reviewMapper, UserRepository userRepository, ReviewRepository reviewRepository, RentalContractRepository rentalContractRepository) {
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.rentalContractRepository = rentalContractRepository;
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
    public ReviewDto createReview(CreateReviewRequestDto dto, long reviewerId) {

        // validacije sve



        ReviewType type = calculateReviewType(dto.getPaymentOk(), dto.getCommunicationOk(), dto.getAgreementOk());

        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> new RuntimeException("Ne postoji osoba koja je ostavila recenziju"));

        RentalContract rc = rentalContractRepository.findById(dto.getContractId()).orElseThrow(() -> new RuntimeException("Ne postoji ugovor za koji je ostavljena recenzija"));

        if(reviewRepository.existsByContractIdAndReviewerId(rc.getId(), reviewerId)){
            throw new RuntimeException("Vec ste stavili ocenu!");
        }

        if(rc.getContractStatus() != ContractStatus.FINISHED){
            throw new RuntimeException("Ugovor nije zavrsen");
        }

        User reviewee = null;
        if(rc.getLessee().getId() == reviewerId){
            reviewee = rc.getAd().getOwner();
        }else if(rc.getAd().getOwner().getId() == reviewerId){
            reviewee = rc.getLessee();
        }else throw new RuntimeException("Niste ucestvovali u ovom ugovoru, ne mozete da ocenite");

        Review review = new Review(
                rc,
                reviewer,
                reviewee,
                dto.getPaymentOk(),
                dto.getCommunicationOk(),
                dto.getAgreementOk(),
                type,
                dto.getComment(),
                LocalDateTime.now()
        );


        if(reviewerId == reviewee.getId()){
            throw new RuntimeException("Ne mozete sami sebi da ostavite ocenu");
        }
        if(type == ReviewType.POSITIVE){
            reviewee.setPositiveReviews(reviewer.getPositiveReviews() + 1);
        }else {
            reviewee.setNegativeReviews(reviewee.getNegativeReviews() + 1);
        }
        reviewRepository.save(review);
        userRepository.save(reviewee);

        return reviewMapper.toDto(review);
    }

}
