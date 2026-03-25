package org.landm.scheduler;

import org.springframework.transaction.annotation.Transactional;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.repository.RentalContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RentalContractScheduler {

    private static final Logger log = LoggerFactory.getLogger(RentalContractScheduler.class);

    private final RentalContractRepository rentalContractRepository;

    public RentalContractScheduler(RentalContractRepository rentalContractRepository) {
        this.rentalContractRepository = rentalContractRepository;
    }

    /**
     * Runs every night at midnight.
     * Expires all REQUESTED or ACCEPTED contracts whose startDate is in the past.
     * ACCEPTED contracts had quantity reserved — marking them EXPIRED frees it
     * automatically since availability is computed only from ACCEPTED/ACTIVE contracts.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueContracts() {
        LocalDate today = LocalDate.now();
        List<ContractStatus> targetStatuses = List.of(ContractStatus.REQUESTED, ContractStatus.ACCEPTED);

        List<RentalContract> overdueContracts = rentalContractRepository
                .findByStatusInAndStartDateBefore(targetStatuses, today);

        for (RentalContract contract : overdueContracts) {
            contract.setContractStatus(ContractStatus.EXPIRED);
        }

        rentalContractRepository.saveAll(overdueContracts);

        if (!overdueContracts.isEmpty()) {
            log.info("Expired {} overdue contract(s) on {}", overdueContracts.size(), today);
        }
    }
}
