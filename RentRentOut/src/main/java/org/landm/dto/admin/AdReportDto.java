package org.landm.dto.admin;

import org.landm.entity.AdReport;

import java.time.LocalDateTime;

public class AdReportDto {
    public Long id;
    public Long adId;
    public String adTitle;
    public Long reporterId;
    public String reporterName;
    public String reason;
    public String note;
    public boolean reviewed;
    public LocalDateTime createdAt;

    public static AdReportDto from(AdReport r) {
        AdReportDto dto = new AdReportDto();
        dto.id = r.getId();
        dto.adId = r.getAd().getId();
        dto.adTitle = r.getAd().getTitle();
        dto.reporterId = r.getReporter().getId();
        dto.reporterName = r.getReporter().getFirstname() + " " + r.getReporter().getLastname();
        dto.reason = r.getReason();
        dto.note = r.getNote();
        dto.reviewed = r.isReviewed();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }
}
