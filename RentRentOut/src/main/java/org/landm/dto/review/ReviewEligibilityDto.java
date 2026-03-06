package org.landm.dto.review;

public class ReviewEligibilityDto {
    private boolean eligible;
    private String reason;

    public ReviewEligibilityDto(boolean eligible, String reason) {
        this.eligible = eligible;
        this.reason = reason;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
