package org.landm.service;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.admin.AdReportDto;
import org.landm.dto.admin.UserCreditSummaryDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.user.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {
    String suspendAd(Long adId);
    Page<UserDto> getAllUsers(Pageable pageable);
    Page<AdPreviewDto> getAllAds(Pageable pageable);
    Page<RentalContractDto> getAllContracts(Pageable pageable);
    String toggleUserEnabled(Long userId);
    String unsuspendAd(Long adId);
    Map<String, Long> getStats();
    Page<UserCreditSummaryDto> getUserCreditSummaries(String search, Pageable pageable);
    Page<AdReportDto> getReports(boolean onlyUnreviewed, Pageable pageable);
    void markReportReviewed(Long reportId);
}
