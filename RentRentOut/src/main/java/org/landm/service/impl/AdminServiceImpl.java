package org.landm.service.impl;

import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.repository.AdRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class AdminServiceImpl implements AdminService {

    private final AdRepository adRepository;
    private final RentalContractRepository rentalContractRepository;

    public AdminServiceImpl(AdRepository adRepository, RentalContractRepository rentalContractRepository) {
        this.adRepository = adRepository;
        this.rentalContractRepository = rentalContractRepository;
    }

    @Override
    @Transactional
    public String suspendAd(long adId) {
        Ad adToSuspend = adRepository.findById(adId).orElseThrow(()-> new RuntimeException("Ad not found by id"));

        if(adToSuspend.getAdStatus() == AdStatus.SUSPENDED_BY_ADMIN){
            throw new RuntimeException("Ad already suspended");
        }
        List<ContractStatus> activeStatuses = List.of(ContractStatus.ACTIVE, ContractStatus.ACCEPTED);
        List<RentalContract> rentalContracts = rentalContractRepository.findActiveContractForAd(adId, activeStatuses);
        for (RentalContract rc : rentalContracts){
            rc.setContractStatus(ContractStatus.CANCELLED_BY_ADMIN);
        }
        rentalContractRepository.saveAll(rentalContracts);


        adToSuspend.setAdStatus(AdStatus.SUSPENDED_BY_ADMIN);
        adRepository.save(adToSuspend);
        return "Ad with ID " + adId + " has been successfully suspended, and " + rentalContracts.size() + " active contract(s) have been cancelled.";
    }
}
