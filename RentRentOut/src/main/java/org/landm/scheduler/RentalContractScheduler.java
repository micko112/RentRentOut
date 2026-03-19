package org.landm.scheduler;

import jakarta.transaction.Transactional;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.repository.RentalContractRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RentalContractScheduler {

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
            System.out.println("[Scheduler] Expired " + overdueContracts.size() + " overdue contract(s) on " + today);
        }
    }
}
