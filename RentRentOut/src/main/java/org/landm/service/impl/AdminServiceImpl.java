package org.landm.service.impl;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.user.UserDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.mapper.AdMapper;
import org.landm.mapper.RentalContractMapper;
import org.landm.mapper.UserMapper;
import org.landm.repository.AdRepository;
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
    private final UserMapper userMapper;
    private final AdMapper adMapper;
    private final RentalContractMapper rentalContractMapper;

    public AdminServiceImpl(
            AdRepository adRepository,
            RentalContractRepository rentalContractRepository,
            UserRepository userRepository,
            UserMapper userMapper,
            AdMapper adMapper,
            RentalContractMapper rentalContractMapper) {
        this.adRepository = adRepository;
        this.rentalContractRepository = rentalContractRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.adMapper = adMapper;
        this.rentalContractMapper = rentalContractMapper;
    }

    @Override
    @Transactional
    public String suspendAd(Long adId) {
        Ad adToSuspend = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found by id"));

        if (adToSuspend.getAdStatus() == AdStatus.SUSPENDED_BY_ADMIN) {
            throw new RuntimeException("Ad already suspended");
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
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<AdPreviewDto> getAllAds(Pageable pageable) {
        return adRepository.findAll(pageable)
                .map(adMapper::toPreviewDto);
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
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

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
                .orElseThrow(() -> new RuntimeException("Ad not found with id: " + adId));

        if (ad.getAdStatus() != AdStatus.SUSPENDED_BY_ADMIN) {
            throw new RuntimeException("Ad with ID " + adId + " is not suspended by admin.");
        }

        ad.setAdStatus(AdStatus.ACTIVE);
        adRepository.save(ad);
        return "Ad with ID " + adId + " has been successfully unsuspended and set to ACTIVE.";
    }

    @Override
    public Map<String, Long> getStats() {
        long totalUsers = userRepository.count();
        long totalAds = adRepository.count();
        long totalContracts = rentalContractRepository.count();
        long activeContracts = rentalContractRepository.countByContractStatusIn(
                List.of(ContractStatus.ACTIVE, ContractStatus.ACCEPTED));

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalAds", totalAds);
        stats.put("totalContracts", totalContracts);
        stats.put("activeContracts", activeContracts);
        return stats;
    }
}
