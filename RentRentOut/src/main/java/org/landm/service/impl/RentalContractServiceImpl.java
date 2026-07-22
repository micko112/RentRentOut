package org.landm.service.impl;

import jakarta.persistence.OptimisticLockException;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.Enums.NotificationType;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.mapper.RentalContractMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.UserRepository;
import org.landm.service.ChatService;
import org.landm.service.NotificationPersistenceService;
import org.landm.service.NotificationService;
import org.landm.service.RentalContractService;
import org.landm.specification.RentalContractSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalContractServiceImpl implements RentalContractService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final RentalContractMapper rentalContractMapper;
    private final RentalContractRepository rentalContractRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final NotificationPersistenceService notifPersistenceService;

    public RentalContractServiceImpl(UserRepository userRepository, AdRepository adRepository,
                                     RentalContractMapper rentalContractMapper, RentalContractRepository rentalContractRepository,
                                     ChatService chatService, NotificationService notificationService,
                                     NotificationPersistenceService notifPersistenceService) {
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.rentalContractMapper = rentalContractMapper;
        this.rentalContractRepository = rentalContractRepository;
        this.chatService = chatService;
        this.notificationService = notificationService;
        this.notifPersistenceService = notifPersistenceService;
    }
    
    @Override
    @Retryable(
    		retryFor = OptimisticLockException.class,
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 100)
    		)
    @Transactional
    public RentalContractDto create(CreateRentalContractRequestDto req, Long userId) {
    	
        User lessee = userRepository.findByIdForCheck(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        


		        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("Datum završetka mora biti posle datuma početka.");
        }

		Ad ad = adRepository.findById(req.getAdId())
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

		if (ad.getOwner().getId().equals(userId)) {
			throw new IllegalArgumentException("Ne možete iznajmiti predmet od samog sebe!");
		}
        List<RentalContract> contractsInInterval = rentalContractRepository.
        		findContractsInDateIntervalIncludingBlocked(req.getAdId(),
        				req.getStartDate(),
        				req.getEndDate());
        
        int availableAmountForAd = getAvailableAmountForInterval(contractsInInterval, req.getStartDate(), req.getEndDate(), 
        				ad.getTotalQuantity());
        
        if(req.getAmount() > availableAmountForAd) {
        	throw new IllegalStateException("Nema dovoljno dostupnih predmeta za traženi period.");
        }
        
        //If these checks are passed app creates and saves offer
        RentalContract rentalToCreate = rentalContractMapper.toEntity(req);
        rentalToCreate.setAd(ad);
        rentalToCreate.setLessee(lessee);
        rentalToCreate.setOfferSender(lessee);
        rentalToCreate.setContractStatus(ContractStatus.REQUESTED);
        rentalToCreate.setAmount(req.getAmount());

        // SIGURNOSNO: backend nezavisno računa cenu iz ad-a i datuma — ignoriše req.agreedPrice
        long days = java.time.temporal.ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        BigDecimal computedPrice = calculateTieredPrice(ad, days);
        BigDecimal quantityMultiplier = BigDecimal.valueOf(req.getAmount() == null ? 1L : req.getAmount());
        rentalToCreate.setAgreedPrice(computedPrice.multiply(quantityMultiplier));

        RentalContract saved = rentalContractRepository.save(rentalToCreate);

        chatService.sendContractRequestMessage(saved);
        notificationService.sendContractRequestEmail(ad.getOwner(), ad, lessee);
        String lesseeFullName = lessee.getFirstname() + " " + lessee.getLastname();
        notifPersistenceService.create(
            ad.getOwner().getId(), NotificationType.CONTRACT_REQUESTED,
            "Novi zahtev za iznajmljivanje",
            lesseeFullName + " je zatražio/la iznajmljivanje predmeta \"" + ad.getTitle() + "\".",
            saved.getId(), "CONTRACT", lesseeFullName
        );

        return rentalContractMapper.toDto(saved);
    }

    @Override
    public List<RentalContract> findByAdIdAndContractStatusIn(Long adId, List<ContractStatus> statusList){
    	return rentalContractRepository.findByAdIdAndContractStatusIn(adId, statusList);
    }
    
    @Transactional
    @Override
    public RentalContractDto updateStatus(Long contractId, UpdateRentalContractStatusRequestDto req, Long userId) {
        RentalContract contract = rentalContractRepository.findByIdPessWriteLock(contractId);
        if(contract == null) throw new IllegalArgumentException("Contract not found.");

        checkPermissions(userId, contract);

        ContractStatus oldStatus = contract.getContractStatus();
        ContractStatus newStatus = req.getNewStatus();

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new IllegalStateException("Promena statusa nije dozvoljena.");
        }
        if (newStatus == ContractStatus.ACCEPTED || newStatus == ContractStatus.FINISHED
        		|| newStatus == ContractStatus.CANCELLED || newStatus == ContractStatus.ACTIVE
        		|| newStatus == ContractStatus.REJECTED
        		|| newStatus == ContractStatus.CANCELLED_AFTER_ACCEPT) {
            changeStatus(contract, req.getNewStatus(), userId);
        }
        if (newStatus == ContractStatus.REQUESTED) {
            handleCounterOffer(contract, req, userId);
            contract.setContractStatus(newStatus);

            User counterOfferSender = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            User owner = contract.getAd().getOwner();
            User lessee = contract.getLessee();
            Long otherId = userId.equals(owner.getId()) ? lessee.getId() : owner.getId();
            String senderFullName = counterOfferSender.getFirstname() + " " + counterOfferSender.getLastname();
            chatService.sendSystemMessage(
                contract.getAd().getId(),
                lessee.getId(),
                owner.getId(),
                counterOfferSender.getFirstname() + " je poslao/la kontra-ponudu za predmet \"" + contract.getAd().getTitle() + "\".",
                userId
            );
            notifPersistenceService.create(
                otherId, NotificationType.CONTRACT_REQUESTED,
                "Nova kontra-ponuda",
                senderFullName + " je poslao/la kontra-ponudu za predmet \"" + contract.getAd().getTitle() + "\".",
                contract.getId(), "CONTRACT", senderFullName
            );
        }

		return rentalContractMapper.toDto(rentalContractRepository.save(contract));
    }

    private static class Event{
    	LocalDate date;
    	Long itemCount;
    	
    	Event(LocalDate date, Long amount){
    		this.date = date;
    		this.itemCount = amount;
    	}
    }
    
    public int getAvailableAmountForInterval(List<RentalContract> contracts, LocalDate startDate, 
    		LocalDate endDate, int totalAmount) {
    	List<Event> events = new ArrayList<>();
    	
    	long avaliableAmountForDates = totalAmount;

    	for(RentalContract rc : contracts) {
    		events.add(new Event(rc.getStartDate(), rc.getAmount()));
    		events.add(new Event(rc.getEndDate().plusDays(1), -rc.getAmount()));
    	}

    	events.sort(Comparator.comparing(e -> e.date));

    	long currUsed = 0;
    	long availableItems = totalAmount;
    	boolean firstInRange = true;

    	for (Event e : events) {
    		if(e.date.isAfter(endDate)) break;

    		if(e.date.isBefore(startDate)) {
    			currUsed += e.itemCount;
    			continue;
    		}

    		// Pre prvog in-range eventa: proveri stanje na osnovu pre-range rezervacija
    		if (firstInRange) {
    			avaliableAmountForDates = Math.min(availableItems - currUsed, avaliableAmountForDates);
    			firstInRange = false;
    		}

    		currUsed += e.itemCount;
    		long availableNow = availableItems - currUsed;
    		avaliableAmountForDates = Math.min(availableNow, avaliableAmountForDates);
    	}

    	// Ako nema in-range eventa, proveri stanje na osnovu pre-range rezervacija
    	if (firstInRange) {
    		avaliableAmountForDates = Math.min(availableItems - currUsed, avaliableAmountForDates);
    	}

    	return (int) avaliableAmountForDates;
    }
    
    private boolean isValidTransition(ContractStatus from, ContractStatus to) {

        return switch (from) {
            case REQUESTED ->
                    to == ContractStatus.ACCEPTED ||
                            to == ContractStatus.REJECTED ||
                            to == ContractStatus.REQUESTED ||       // kontra-ponuda
                            to == ContractStatus.CANCELLED;         // lessee otkazuje pre prihvatanja

            case ACCEPTED ->
                    to == ContractStatus.ACTIVE ||
                    to == ContractStatus.CANCELLED_AFTER_ACCEPT;    // otkazivanje prihvaćenog

            case ACTIVE ->
                    to == ContractStatus.FINISHED ||
                    to == ContractStatus.CANCELLED_AFTER_ACCEPT;    // raskid aktivnog

            case REJECTED, FINISHED, CANCELLED, CANCELLED_AFTER_ACCEPT, DELETED, EXPIRED -> false;

            default -> false;
        };
    }
    private void checkPermissions(Long userId, RentalContract contract){
        boolean isOwner = contract.getAd().getOwner().getId().equals(userId);
        boolean isLessee = contract.getLessee().getId().equals(userId);
        if(!isOwner && !isLessee){
            throw new AccessDeniedException("User is not in contract");
        }
    }
    
    private void changeStatus(RentalContract contract, ContractStatus newStatus, Long userId){
        Ad ad = contract.getAd();
        ContractStatus oldStatus = contract.getContractStatus();

        if(oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.ACCEPTED){

            if (contract.getEndDate().isBefore(LocalDate.now())) {
                throw new IllegalStateException("Ne možete prihvatiti zahtev čiji period je već istekao.");
            }

			Ad contrAd = adRepository.findByIdForUpdate(ad.getId())
				.orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        	User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())// lessee - zakljucan
        			.orElseThrow(() -> new UserNotFoundException("User not found"));
        	
        	User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId()) // owner - zakljucan
        			.orElseThrow(() -> new UserNotFoundException("User not found"));

        	LocalDate startDate = contract.getStartDate();
        	LocalDate endDate = contract.getEndDate();
        	List<ContractStatus> statusses = new ArrayList<>();
        	statusses.add(ContractStatus.ACCEPTED);
        	statusses.add(ContractStatus.ACTIVE);
        	List<RentalContract> contracts = rentalContractRepository.findActiveContractForAd(ad.getId(), statusses);
        	
        	int availableQuantity = getAvailableAmountForInterval(contracts, startDate, endDate, contrAd.getTotalQuantity());
        	
        	if(availableQuantity < contract.getAmount()){
                throw new IllegalStateException("Nema dovoljno dostupnih predmeta za ovaj period.");
            }
        	
        	//sve provere su prosle, promena statusa i cuvanje ugovora

        	contract.setContractStatus(ContractStatus.ACCEPTED);
            rentalContractRepository.save(contract);

            String ownerName = owner.getFirstname();
            chatService.sendSystemMessage(
                ad.getId(),
                lessee.getId(),
                owner.getId(),
                ownerName + " je prihvatio/la vaš zahtev za iznajmljivanje.",
                userId
            );
            notificationService.sendContractAcceptedEmail(lessee, ad);
            notifPersistenceService.create(
                lessee.getId(), NotificationType.CONTRACT_ACCEPTED,
                "Zahtev prihvaćen",
                owner.getFirstname() + " " + owner.getLastname() + " je prihvatio/la vaš zahtev za iznajmljivanje predmeta \"" + ad.getTitle() + "\".",
                contract.getId(), "CONTRACT", owner.getFirstname() + " " + owner.getLastname()
            );
        }
		if (oldStatus == ContractStatus.ACCEPTED && newStatus == ContractStatus.ACTIVE) {
			contract.setContractStatus(ContractStatus.ACTIVE);
			rentalContractRepository.save(contract);
			User owner = contract.getAd().getOwner();
			User lessee = contract.getLessee();
			String actorName = userId.equals(owner.getId())
					? owner.getFirstname() + " " + owner.getLastname()
					: lessee.getFirstname() + " " + lessee.getLastname();
			notifPersistenceService.create(
				lessee.getId(), NotificationType.CONTRACT_ACTIVE,
				"Iznajmljivanje počelo",
				"Iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\" je označeno kao aktivno.",
				contract.getId(), "CONTRACT", actorName
			);
			notifPersistenceService.create(
				owner.getId(), NotificationType.CONTRACT_ACTIVE,
				"Iznajmljivanje počelo",
				"Iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\" je označeno kao aktivno.",
				contract.getId(), "CONTRACT", actorName
			);
		}
        if (oldStatus == ContractStatus.ACTIVE &&  newStatus == ContractStatus.FINISHED) {
            contract.setContractStatus(newStatus);
            rentalContractRepository.save(contract);
            User owner = contract.getAd().getOwner();
            User lessee = contract.getLessee();
            String actorName = userId.equals(owner.getId())
                    ? owner.getFirstname() + " " + owner.getLastname()
                    : lessee.getFirstname() + " " + lessee.getLastname();
            notifPersistenceService.create(
                lessee.getId(), NotificationType.CONTRACT_FINISHED,
                "Iznajmljivanje završeno",
                "Iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\" je označeno kao završeno.",
                contract.getId(), "CONTRACT", actorName
            );
            notifPersistenceService.create(
                owner.getId(), NotificationType.CONTRACT_FINISHED,
                "Iznajmljivanje završeno",
                "Iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\" je označeno kao završeno.",
                contract.getId(), "CONTRACT", actorName
            );
        }


        if (oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.REJECTED) {
            User lessor = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            User contractLessee = contract.getLessee();
            contract.setContractStatus(ContractStatus.REJECTED);
            rentalContractRepository.save(contract);

            chatService.sendSystemMessage(
                contract.getAd().getId(),
                contractLessee.getId(),
                lessor.getId(),
                lessor.getFirstname() + " je odbio/la vaš zahtev za iznajmljivanje.",
                userId
            );
            notificationService.sendContractRejectedEmail(contractLessee, contract.getAd());
            notifPersistenceService.create(
                contractLessee.getId(), NotificationType.CONTRACT_REJECTED,
                "Zahtev odbijen",
                lessor.getFirstname() + " " + lessor.getLastname() + " je odbio/la vaš zahtev za iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\".",
                contract.getId(), "CONTRACT", lessor.getFirstname() + " " + lessor.getLastname()
            );
        }

        if (oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.CANCELLED) {
            User lessee = contract.getLessee();
            if (!lessee.getId().equals(userId)) {
                throw new AccessDeniedException("Samo zakupac može povući zahtev za iznajmljivanje.");
            }
            User owner = contract.getAd().getOwner();
            contract.setContractStatus(ContractStatus.CANCELLED);
            rentalContractRepository.save(contract);
            chatService.sendSystemMessage(
                contract.getAd().getId(),
                lessee.getId(),
                owner.getId(),
                lessee.getFirstname() + " je povukao/la zahtev za iznajmljivanje.",
                userId
            );
            notifPersistenceService.create(
                owner.getId(), NotificationType.CONTRACT_CANCELLED,
                "Zahtev povučen",
                lessee.getFirstname() + " " + lessee.getLastname() + " je povukao/la zahtev za iznajmljivanje predmeta \"" + contract.getAd().getTitle() + "\".",
                contract.getId(), "CONTRACT", lessee.getFirstname() + " " + lessee.getLastname()
            );
        }

        if (oldStatus == ContractStatus.ACCEPTED && newStatus == ContractStatus.CANCELLED_AFTER_ACCEPT) {
            User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            String cancellerName = userId.equals(owner.getId()) ? owner.getFirstname() : lessee.getFirstname();
            String cancellerFullName = userId.equals(owner.getId())
                    ? owner.getFirstname() + " " + owner.getLastname()
                    : lessee.getFirstname() + " " + lessee.getLastname();
            Long otherId = userId.equals(owner.getId()) ? lessee.getId() : owner.getId();
            contract.setContractStatus(ContractStatus.CANCELLED_AFTER_ACCEPT);
            rentalContractRepository.save(contract);

            // Zalihe se automatski oslobadjaju — dostupnost se racuna samo iz ACCEPTED/ACTIVE ugovora
            chatService.sendSystemMessage(
                contract.getAd().getId(),
                lessee.getId(),
                owner.getId(),
                cancellerName + " je otkazao/la prihvaćeni ugovor. Obe strane mogu da ostave ocenu.",
                userId
            );
            notifPersistenceService.create(
                otherId, NotificationType.CONTRACT_CANCELLED,
                "Ugovor otkazan",
                cancellerFullName + " je otkazao/la prihvaćeni ugovor za predmet \"" + contract.getAd().getTitle() + "\".",
                contract.getId(), "CONTRACT", cancellerFullName
            );
        }

        if (oldStatus == ContractStatus.ACTIVE && newStatus == ContractStatus.CANCELLED_AFTER_ACCEPT) {
            User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            String cancellerName = userId.equals(owner.getId()) ? owner.getFirstname() : lessee.getFirstname();
            String cancellerFullName = userId.equals(owner.getId())
                    ? owner.getFirstname() + " " + owner.getLastname()
                    : lessee.getFirstname() + " " + lessee.getLastname();
            Long otherId = userId.equals(owner.getId()) ? lessee.getId() : owner.getId();
            contract.setContractStatus(ContractStatus.CANCELLED_AFTER_ACCEPT);
            rentalContractRepository.save(contract);

            chatService.sendSystemMessage(
                contract.getAd().getId(),
                lessee.getId(),
                owner.getId(),
                cancellerName + " je raskinuo/la aktivan ugovor. Obe strane mogu da ostave ocenu.",
                userId
            );
            notifPersistenceService.create(
                otherId, NotificationType.CONTRACT_CANCELLED,
                "Ugovor raskinut",
                cancellerFullName + " je raskinuo/la aktivan ugovor za predmet \"" + contract.getAd().getTitle() + "\".",
                contract.getId(), "CONTRACT", cancellerFullName
            );
        }

    }

    private void handleCounterOffer(RentalContract contract, UpdateRentalContractStatusRequestDto req, Long userId) {
        if (req.getNewStatus() != ContractStatus.REQUESTED || contract.getContractStatus() != ContractStatus.REQUESTED) {
            return;
        }
        if (contract.getOfferSender().getId().equals(userId)) {
            throw new IllegalStateException("Ne možete slati kontra-ponudu sami sebi.");
        }

        contract.setOfferSender(
                userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User not found!")));

        // Ako su novi datumi prosleđeni, proveri dostupnost i ažuriraj
        if (req.getNewStartDate() != null && req.getNewEndDate() != null) {
            if (req.getNewEndDate().isBefore(req.getNewStartDate())) {
                throw new IllegalArgumentException("Datum završetka mora biti posle datuma početka.");
            }
            List<ContractStatus> activeStatuses = List.of(ContractStatus.ACCEPTED, ContractStatus.ACTIVE);
            List<RentalContract> activeContracts = rentalContractRepository
                    .findActiveContractForAd(contract.getAd().getId(), activeStatuses);

            int available = getAvailableAmountForInterval(
                    activeContracts, req.getNewStartDate(), req.getNewEndDate(),
                    contract.getAd().getTotalQuantity());

            if (contract.getAmount() > available) {
                throw new IllegalStateException("Nema dovoljno dostupnih predmeta za tražene datume.");
            }
            contract.setStartDate(req.getNewStartDate());
            contract.setEndDate(req.getNewEndDate());
        }

        if (req.getNewPrice() == null) {
            throw new IllegalArgumentException("Morate ponuditi novu cenu u kontra-ponudi!");
        }
        contract.setAgreedPrice(req.getNewPrice());
    }

	@Override
	public RentalContractDto getRentalContractById(Long rentalId, Long requestingUserId) {
		RentalContract contract = rentalContractRepository.findById(rentalId)
				.orElseThrow(() -> new IllegalArgumentException("Contract not found"));
		checkPermissions(requestingUserId, contract);
		return rentalContractMapper.toDto(contract);
	}

	@Override
	public List<RentalContractDto> getAll(Long userId) {
		return rentalContractRepository.findAllByUser(userId)
				.stream()
				.map(rentalContractMapper::toDto)
				.toList();
	}
	
	@Override
	public Page<RentalContractDto> search(Long userId, boolean isAdmin, RentalContractSearchDto searchDto) {
		
		String sortBy = mapSortField(searchDto.getSortBy());
		
		Sort sort = searchDto.isDescending() 
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);
		
		return rentalContractRepository
				.findAll(RentalContractSpecification.search(userId, isAdmin, searchDto), pageable)
				.map(rentalContractMapper::toDto)
				;
	}
	
	private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
		"startDate", "endDate", "agreedPrice", "contractStatus", "id"
	);

	public String mapSortField(String frontendField) {
		return switch(frontendField) {
		case "adTitle" -> "ad.title";
		case "firstname" -> "ad.owner.firstname";
		default -> ALLOWED_SORT_FIELDS.contains(frontendField) ? frontendField : "startDate";
		};
	}

	public boolean isActiveOrAccepted(ContractStatus status) {
		return (status == ContractStatus.ACCEPTED || status == ContractStatus.ACTIVE);
	}
	
	@Transactional
	@Override
	public String delete(Long userId, Long rentalId) {
		
		RentalContract currContr = rentalContractRepository.findById(rentalId)
				.orElseThrow(() -> new IllegalArgumentException("Contract not found"));
		
		if(!currContr.getLessee().getId().equals(userId)) {
			throw new AccessDeniedException("Nemate dozvolu za brisanje ovog ugovora.");
		}

		if(isActiveOrAccepted(currContr.getContractStatus())) {
			throw new IllegalStateException("Ne možete obrisati aktivan ugovor.");
		}

		if(currContr.getContractStatus() != ContractStatus.DELETED) {
			currContr.setContractStatus(ContractStatus.DELETED);
		}else {
			throw new IllegalStateException("Ugovor je već obrisan.");
		}
		rentalContractRepository.save(currContr);
		
		return "Successfully deleted your contract!";
	}
	
	@Override
	public void markToAdDeleted(Long adId) {
		rentalContractRepository.markToAdDeleted(adId);
		
	}

	@Transactional
	@Override
	public RentalContractDto blockDates(CreateRentalContractRequestDto req, Long userId) {
		if (req.getEndDate().isBefore(req.getStartDate())) {
			throw new IllegalArgumentException("Datum završetka mora biti posle datuma početka.");
		}
		User owner = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
		Ad ad = adRepository.findById(req.getAdId()).orElseThrow(()-> new IllegalArgumentException("Ad not found"));
		if (!ad.getOwner().getId().equals(userId)) {
			throw new AccessDeniedException("Samo vlasnik može blokirati datume.");
		}
		RentalContract blockRecord = rentalContractMapper.toEntity(req);
		blockRecord.setAd(ad);
		blockRecord.setLessee(owner);
		blockRecord.setOfferSender(owner);
		blockRecord.setAgreedPrice(BigDecimal.ZERO);
		blockRecord.setAmount(req.getAmount());
		blockRecord.setCurrency(req.getCurrency());
		blockRecord.setContractStatus(ContractStatus.BLOCKED_BY_OWNER);

		return rentalContractMapper.toDto(rentalContractRepository.save(blockRecord));
	}

	@Override
	public List<RentalContractDto> getFinishedWithUser(Long myId, Long otherId) {
		return rentalContractRepository.findFinishedBetweenUsers(myId, otherId)
				.stream()
				.map(rentalContractMapper::toDto)
				.collect(Collectors.toList());
	}

	@Recover
	public RentalContractDto recover(OptimisticLockException e) {
		throw new IllegalStateException("Vaš zahtev nije mogao biti obrađen zbog paralelnih izmena. Pokušajte ponovo.");
	}

	/**
	 * Backend-side price calculator. Ne veruje frontend-u — nezavisno računa cenu
	 * na osnovu ad.price / pricePerWeek / pricePerMonth i broja dana.
	 * Mora se poklapati sa frontend logikom u rental-calendar.component.ts.
	 */
	private BigDecimal calculateTieredPrice(Ad ad, long days) {
		if (days <= 0 || ad.getPrice() == null) return BigDecimal.ZERO;
		BigDecimal price = ad.getPrice();
		BigDecimal weekly = ad.getPricePerWeek();
		BigDecimal monthly = ad.getPricePerMonth();
		PriceInterval interval = ad.getPriceInterval();

		if (interval == PriceInterval.PER_MONTH) {
			if (monthly != null) {
				long wholeMonths = days / 30;
				long rem = days % 30;
				BigDecimal total = monthly.multiply(BigDecimal.valueOf(wholeMonths));
				if (rem > 0) {
					// pro-rata dnevna cena za ostatak
					BigDecimal dailyEquiv = monthly.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
					total = total.add(dailyEquiv.multiply(BigDecimal.valueOf(rem)));
				}
				return total.setScale(0, java.math.RoundingMode.HALF_UP);
			}
			return price.multiply(BigDecimal.valueOf(days));
		}

		// PER_DAY (default za većinu oglasa) — tiered
		long remaining = days;
		BigDecimal total = BigDecimal.ZERO;

		if (monthly != null && remaining >= 30) {
			long months = remaining / 30;
			total = total.add(monthly.multiply(BigDecimal.valueOf(months)));
			remaining -= months * 30;
		}
		if (weekly != null && remaining >= 7) {
			long weeks = remaining / 7;
			total = total.add(weekly.multiply(BigDecimal.valueOf(weeks)));
			remaining -= weeks * 7;
		}
		if (remaining > 0) {
			total = total.add(price.multiply(BigDecimal.valueOf(remaining)));
		}
		return total;
	}

}
