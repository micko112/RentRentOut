package org.landm.scheduler;

import org.springframework.transaction.annotation.Transactional;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.Enums.NotificationType;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.repository.RentalContractRepository;
import org.landm.service.NotificationPersistenceService;
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
    private final NotificationPersistenceService notifPersistenceService;

    public RentalContractScheduler(RentalContractRepository rentalContractRepository,
                                   NotificationPersistenceService notifPersistenceService) {
        this.rentalContractRepository = rentalContractRepository;
        this.notifPersistenceService = notifPersistenceService;
    }

    /**
     * Runs every night at 00:00.
     * Expires REQUESTED contracts whose startDate is in the past (owner never responded).
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueRequestedContracts() {
        LocalDate today = LocalDate.now();

        List<RentalContract> overdue = rentalContractRepository
                .findByStatusInAndStartDateBefore(List.of(ContractStatus.REQUESTED), today);

        for (RentalContract contract : overdue) {
            contract.setContractStatus(ContractStatus.EXPIRED);
        }

        rentalContractRepository.saveAll(overdue);

        if (!overdue.isEmpty()) {
            log.info("Expired {} overdue REQUESTED contract(s) on {}", overdue.size(), today);
        }
    }

    /**
     * Runs every night at 00:01.
     * Transitions ACCEPTED contracts to ACTIVE when their startDate has arrived.
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void activateStartedContracts() {
        LocalDate today = LocalDate.now();

        List<RentalContract> toActivate = rentalContractRepository
                .findAcceptedContractsStartingOnOrBefore(today);

        for (RentalContract contract : toActivate) {
            contract.setContractStatus(ContractStatus.ACTIVE);
            User owner = contract.getAd().getOwner();
            User lessee = contract.getLessee();
            String adTitle = contract.getAd().getTitle();

            notifPersistenceService.create(
                lessee.getId(), NotificationType.CONTRACT_ACTIVE,
                "Iznajmljivanje počelo",
                "Iznajmljivanje predmeta \"" + adTitle + "\" je počelo danas.",
                contract.getId(), "CONTRACT", owner.getFirstname() + " " + owner.getLastname()
            );
            notifPersistenceService.create(
                owner.getId(), NotificationType.CONTRACT_ACTIVE,
                "Iznajmljivanje počelo",
                "Iznajmljivanje predmeta \"" + adTitle + "\" je počelo danas.",
                contract.getId(), "CONTRACT", lessee.getFirstname() + " " + lessee.getLastname()
            );
        }

        rentalContractRepository.saveAll(toActivate);

        if (!toActivate.isEmpty()) {
            log.info("Activated {} contract(s) on {}", toActivate.size(), today);
        }
    }

    /**
     * Runs every night at 00:02.
     * Transitions ACTIVE contracts to FINISHED when their endDate has passed.
     */
    @Scheduled(cron = "0 2 0 * * *")
    @Transactional
    public void finishEndedContracts() {
        LocalDate today = LocalDate.now();

        List<RentalContract> toFinish = rentalContractRepository
                .findActiveContractsEndedBefore(today);

        for (RentalContract contract : toFinish) {
            contract.setContractStatus(ContractStatus.FINISHED);
            User owner = contract.getAd().getOwner();
            User lessee = contract.getLessee();
            String adTitle = contract.getAd().getTitle();

            notifPersistenceService.create(
                lessee.getId(), NotificationType.CONTRACT_FINISHED,
                "Iznajmljivanje završeno",
                "Iznajmljivanje predmeta \"" + adTitle + "\" je završeno.",
                contract.getId(), "CONTRACT", owner.getFirstname() + " " + owner.getLastname()
            );
            notifPersistenceService.create(
                owner.getId(), NotificationType.CONTRACT_FINISHED,
                "Iznajmljivanje završeno",
                "Iznajmljivanje predmeta \"" + adTitle + "\" je završeno.",
                contract.getId(), "CONTRACT", lessee.getFirstname() + " " + lessee.getLastname()
            );
        }

        rentalContractRepository.saveAll(toFinish);

        if (!toFinish.isEmpty()) {
            log.info("Finished {} contract(s) on {}", toFinish.size(), today);
        }
    }
}
