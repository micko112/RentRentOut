package org.landm.dto.user;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.review.ReviewDto;
import org.springframework.data.domain.Page;

public class PublicProfileDto {
    private UserProfileDto userInfo;
    private Page<AdPreviewDto> ads;
    private Page<ReviewDto> reviews;

    public PublicProfileDto(UserProfileDto userInfo, Page<AdPreviewDto> ads, Page<ReviewDto> reviews) {
        this.userInfo = userInfo;
        this.ads = ads;
        this.reviews = reviews;
    }

    public UserProfileDto getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserProfileDto userInfo) {
        this.userInfo = userInfo;
    }

    public Page<AdPreviewDto> getAds() {
        return ads;
    }

    public void setAds(Page<AdPreviewDto> ads) {
        this.ads = ads;
    }

    public Page<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(Page<ReviewDto> reviews) {
        this.reviews = reviews;
    }
}
