package org.landm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.landm.mapper.ReviewMapper;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.ReviewRepository;
import org.landm.repository.UserRepository;
import org.landm.service.impl.ReviewServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock private ReviewMapper reviewMapper;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private RentalContractRepository rentalContractRepository;
    @Mock private NotificationPersistenceService notifPersistenceService;

    @InjectMocks
    private ReviewServiceImpl reviewService;



    @Test
    void calculateReviewType_allNo_returnsNegative() {
        ReviewType result = reviewService.calculateReviewType(
                ReviewOption.NO, ReviewOption.NO, ReviewOption.NO);
        assertThat(result).isEqualTo(ReviewType.NEGATIVE);
    }

    @Test
    void calculateReviewType_twoNoOneCouldBeBetter_returnsNegative() {
        ReviewType result = reviewService.calculateReviewType(
                ReviewOption.NO, ReviewOption.NO, ReviewOption.COULD_BE_BETTER);
        assertThat(result).isEqualTo(ReviewType.NEGATIVE);
    }

    @Test
    void calculateReviewType_oneNoTwoCouldBeBetter_returnsNegative() {
        ReviewType result = reviewService.calculateReviewType(
                ReviewOption.NO, ReviewOption.COULD_BE_BETTER, ReviewOption.COULD_BE_BETTER);
        assertThat(result).isEqualTo(ReviewType.NEGATIVE);
    }

    @Test
    void calculateReviewType_mixedButNotNegativeThreshold_returnsPositive() {
        // 1 NO + 1 COULD_BE_BETTER + 1 YES — not enough for NEGATIVE
        ReviewType result = reviewService.calculateReviewType(
                ReviewOption.NO, ReviewOption.COULD_BE_BETTER, ReviewOption.YES);
        assertThat(result).isEqualTo(ReviewType.POSITIVE);
    }



    @Test
    void findContractWithUser_noFinishedContracts_returnsNull() {
        when(rentalContractRepository.findFinishedBetweenUsers(1L, 2L))
                .thenReturn(List.of());

        Long result = reviewService.findContractWithUser(1L, 2L);

        assertThat(result).isNull();
    }
}
