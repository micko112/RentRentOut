package org.landm.service.impl;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.admin.AdReportDto;
import org.landm.dto.admin.UserCreditSummaryDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.user.UserDto;
import org.landm.entity.Ad;
import org.landm.entity.AdReport;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.mapper.AdMapper;
import org.landm.mapper.RentalContractMapper;
import org.landm.exception.UserNotFoundException;
import org.landm.mapper.UserMapper;
import org.landm.repository.AdReportRepository;
import org.landm.repository.AdRepository;
import org.landm.repository.CreditTransactionRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.UserRepository;
import org.landm.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdRepository adRepository;
    private final RentalContractRepository rentalContractRepository;
    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final AdReportRepository adReportRepository;
    private final UserMapper userMapper;
    private final AdMapper adMapper;
    private final RentalContractMapper rentalContractMapper;

    public AdminServiceImpl(
            AdRepository adRepository,
            RentalContractRepository rentalContractRepository,
            UserRepository userRepository,
            CreditTransactionRepository creditTransactionRepository,
            AdReportRepository adReportRepository,
            UserMapper userMapper,
            AdMapper adMapper,
            RentalContractMapper rentalContractMapper) {
        this.adRepository = adRepository;
        this.rentalContractRepository = rentalContractRepository;
        this.userRepository = userRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.adReportRepository = adReportRepository;
        this.userMapper = userMapper;
        this.adMapper = adMapper;
        this.rentalContractMapper = rentalContractMapper;
    }

    @Override
    @Transactional
    public String suspendAd(Long adId) {
        Ad adToSuspend = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        if (adToSuspend.getAdStatus() == AdStatus.SUSPENDED_BY_ADMIN) {
            throw new IllegalStateException("Oglas je već suspendovan.");
        }
        List<ContractStatus> activeStatuses = List.of(ContractStatus.ACTIVE, ContractStatus.ACCEPTED);
        List<RentalContract> rentalContracts = rentalContractRepository.findActiveContractForAd(adId, activeStatuses);
        for (RentalContract rc : rentalContracts) {
            rc.setContractStatus(ContractStatus.CANCELLED_BY_ADMIN);
        }
        rentalContractRepository.saveAll(rentalContracts);

        adToSuspend.setAdStatus(AdStatus.SUSPENDED_BY_ADMIN);
        adRepository.save(adToSuspend);
        return "Ad with ID " + adId + " has been successfully suspended, and "
                + rentalContracts.size() + " active contract(s) have been cancelled.";
    }

    @Override
    public Page<UserDto> getAllUsers(String search, Pageable pageable) {
        String s = (search != null && !search.isBlank()) ? search.trim() : null;
        return userRepository.searchUsers(s, pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<AdPreviewDto> getAllAds(String search, String status, Pageable pageable) {
        String s = (search != null && !search.isBlank()) ? search.trim() : null;
        AdStatus adStatus = null;
        if (status != null && !status.isBlank()) {
            try { adStatus = AdStatus.valueOf(status.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        return adRepository.searchAds(s, adStatus, pageable)
                .map(adMapper::toPreviewDto);
    }

    @Override
    @Transactional
    public void deleteAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Oglas nije pronađen."));
        adRepository.delete(ad);
    }

    @Override
    public Page<RentalContractDto> getAllContracts(Pageable pageable) {
        return rentalContractRepository.findAll(pageable)
                .map(rentalContractMapper::toDto);
    }

    @Override
    @Transactional
    public String toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean newState = !user.isEnabled();
        user.setEnabled(newState);
        userRepository.save(user);

        String action = newState ? "enabled" : "disabled";
        return "User with ID " + userId + " has been " + action + ".";
    }

    @Override
    @Transactional
    public String unsuspendAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        if (ad.getAdStatus() != AdStatus.SUSPENDED_BY_ADMIN) {
            throw new IllegalStateException("Oglas nije suspendovan od strane admina.");
        }

        ad.setAdStatus(AdStatus.ACTIVE);
        adRepository.save(ad);
        return "Ad with ID " + adId + " has been successfully unsuspended and set to ACTIVE.";
    }

    @Override
    public Map<String, Long> getStats() {
        long totalUsers     = userRepository.count();
        long totalAds       = adRepository.count();
        long activeAds      = adRepository.findAllByAdStatus(AdStatus.ACTIVE,
                                  org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalContracts = rentalContractRepository.count();
        long activeContracts = rentalContractRepository.countByContractStatusIn(
                                  List.of(ContractStatus.ACTIVE, ContractStatus.ACCEPTED));
        long pendingReports = adReportRepository.countByReviewedFalse();
        long totalRevenue   = creditTransactionRepository.findAll().stream()
                                  .filter(t -> t.getAmount() != null && t.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
                                  .mapToLong(t -> t.getAmount().longValue())
                                  .sum();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalUsers",      totalUsers);
        stats.put("totalAds",        totalAds);
        stats.put("activeAds",       activeAds);
        stats.put("totalContracts",  totalContracts);
        stats.put("activeContracts", activeContracts);
        stats.put("pendingReports",  pendingReports);
        stats.put("totalRevenue",    totalRevenue);
        return stats;
    }

    @Override
    public Page<UserCreditSummaryDto> getUserCreditSummaries(String search, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return creditTransactionRepository.findUserCreditSummaries(searchParam, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdReportDto> getReports(boolean onlyUnreviewed, Pageable pageable) {
        if (onlyUnreviewed) {
            return adReportRepository.findAllByReviewedFalseOrderByCreatedAtDesc(pageable)
                    .map(AdReportDto::from);
        }
        return adReportRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdReportDto::from);
    }

    @Override
    @Transactional
    public String toggleIdentified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        boolean newState = !user.isIdentified();
        user.setIdentified(newState);
        userRepository.save(user);
        return newState ? "verified" : "unverified";
    }

    @Override
    @Transactional
    public void markReportReviewed(Long reportId) {
        AdReport report = adReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Prijava nije pronađena."));
        report.setReviewed(true);
        adReportRepository.save(report);
    }
}
