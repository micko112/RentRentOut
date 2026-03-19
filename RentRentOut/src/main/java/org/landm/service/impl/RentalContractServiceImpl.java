package org.landm.service.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.mapper.RentalContractMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.entity.Enums.NotificationType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RentalContractServiceImpl implements RentalContractService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final RentalContractMapper rentalContractMapper;
    private final RentalContractRepository rentalContractRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final NotificationPersistenceService notifPersistenceService;

    public RentalContractServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, AdRepository adRepository,
                                     RentalContractMapper rentalContractMapper, RentalContractRepository rentalContractRepository,
                                     ChatService chatService, NotificationService notificationService,
                                     NotificationPersistenceService notifPersistenceService) {
        this.jwtUtil = jwtUtil;
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
    		value = OptimisticLockException.class,
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 100)
    		)
    @Transactional
    public RentalContractDto create(CreateRentalContractRequestDto req, Long userId) {
    	
        User lessee = userRepository.findByIdForCheck(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Found user");

				Ad ad = adRepository.findById(req.getAdId())
                .orElseThrow(() -> new RuntimeException("Ad not found"));

		if (ad.getOwner().getId().equals(userId)) {
			throw new RuntimeException("Ne možete iznajmiti predmet od samog sebe!");
		}
    	// Should check for amount of available items and allow sending offer only if there are some
        List<RentalContract> contractsInInterval = rentalContractRepository.
        		findContractsInDateInterval(req.getAdId(), 
        				req.getStartDate(), 
        				req.getEndDate());
        
        int availableAmountForAd = getAvailableAmountForInterval(contractsInInterval, req.getStartDate(), req.getEndDate(), 
        				ad.getTotalQuantity());
        
        if(req.getAmount() > availableAmountForAd) {
        	throw new RuntimeException("No enough available items for this Ad.");
        }
        
        //If these checks are passed app creates and saves offer
        RentalContract rentalToCreate = rentalContractMapper.toEntity(req);
        rentalToCreate.setAd(ad);
        rentalToCreate.setLessee(lessee);
        rentalToCreate.setOfferSender(lessee);
        rentalToCreate.setContractStatus(ContractStatus.REQUESTED);
        rentalToCreate.setAmount(req.getAmount());
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
    public RentalContractDto updateStatus(Long contractId, UpdateRentalContractStatusRequestDto req, 
    		Long userId) {

		// Ugovor mora da se zakljuca - moze optimistic_force_increment? ili pessimistic_write
    	
    	// Korisnik takodje mora da se zakljuca, skidanje para 
    	
        RentalContract contract = rentalContractRepository.findByIdPessWriteLock(contractId);
        if(contract == null) throw new RuntimeException("Error searching contract!");

        checkPermissions(userId, contract);

        ContractStatus oldStatus = contract.getContractStatus();
        ContractStatus newStatus = req.getNewStatus();

		System.out.println("DEBUG: Prelaz sa " + oldStatus + " na " + newStatus);

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition");
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
        }

		return rentalContractMapper.toDto(rentalContractRepository.save(contract)); //bolje je da se sacuva
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
    	
    	int avaliableAmountForDates = totalAmount;
    	
    	for(RentalContract rc : contracts) {
    		events.add(new Event(rc.getStartDate(), rc.getAmount()));
    		
    		events.add(new Event(rc.getEndDate().plusDays(1), -rc.getAmount()));
    	}
    	
    	events.sort(Comparator.comparing(e -> e.date));
    	
    	int currUsed = 0;
    	int availableItems = totalAmount;
    	
    	for (Event e : events) {
    		if(e.date.isAfter(endDate)) break;
    		
    		if(e.date.isBefore(startDate)) {
    			currUsed += e.itemCount;
    			continue;
    		}
    		
    		currUsed += e.itemCount;
    		int availableNow = availableItems - currUsed;
    		avaliableAmountForDates = Math.min(availableNow, avaliableAmountForDates);
    		
    	}
    	
    	return avaliableAmountForDates;
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
        boolean isOwner = contract.getAd().getOwner().getId() == userId;
        boolean isLessee = contract.getLessee().getId() == userId;
        if(!isOwner && !isLessee){
            throw new AccessDeniedException("User is not in contract");
        }
    }
    
    private void changeStatus(RentalContract contract, ContractStatus newStatus, Long userId){
        Ad ad = contract.getAd();
        ContractStatus oldStatus = contract.getContractStatus();

        if(oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.ACCEPTED){ // ovde treba logika za prihvatanje - provera i  

			Ad contrAd = adRepository.findByIdForUpdate(ad.getId());

        	User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())// lessee - zakljucan
        			.orElseThrow(() -> new RuntimeException("No user found!"));
        	
        	User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId()) // owner - zakljucan
        			.orElseThrow(() -> new RuntimeException("No user found!"));

        	LocalDate startDate = contract.getStartDate();
        	LocalDate endDate = contract.getEndDate();
        	List<ContractStatus> statusses = new ArrayList<>();
        	statusses.add(ContractStatus.ACCEPTED);
        	statusses.add(ContractStatus.ACTIVE);
        	List<RentalContract> contracts = rentalContractRepository.findActiveContractForAd(ad.getId(), statusses);
        	
        	int availableQuantity = getAvailableAmountForInterval(contracts, startDate, endDate, contrAd.getTotalQuantity());
        	
        	if(availableQuantity < contract.getAmount()){ // smanjivanje qntity i logika za proveru i smanjivanje para korisniku
                throw new RuntimeException("Not enough items."); // i povecavanje para korisniku koji je vlasnik
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
		}
        if (oldStatus == ContractStatus.ACTIVE &&  newStatus == ContractStatus.FINISHED) {
            contract.setContractStatus(newStatus);
            rentalContractRepository.save(contract);
        }


        if (oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.REJECTED) {
            User lessor = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found!"));
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

        if (oldStatus == ContractStatus.ACCEPTED && newStatus == ContractStatus.CANCELLED_AFTER_ACCEPT) {
            User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            String cancellerName = userId.equals(owner.getId()) ? owner.getFirstname() : lessee.getFirstname();
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
        }

        if (oldStatus == ContractStatus.ACTIVE && newStatus == ContractStatus.CANCELLED_AFTER_ACCEPT) {
            User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            String cancellerName = userId.equals(owner.getId()) ? owner.getFirstname() : lessee.getFirstname();
            contract.setContractStatus(ContractStatus.CANCELLED_AFTER_ACCEPT);
            rentalContractRepository.save(contract);

            chatService.sendSystemMessage(
                contract.getAd().getId(),
                lessee.getId(),
                owner.getId(),
                cancellerName + " je raskinuo/la aktivan ugovor. Obe strane mogu da ostave ocenu.",
                userId
            );
        }

        if (oldStatus == ContractStatus.ACTIVE && newStatus == ContractStatus.CANCELLED) {
        	// stari blok — ne moze se vise triggerovati jer ACTIVE->CANCELLED nije validan prelaz
        	// ostavljeno radi kompajlabilnosti, mrtav kod
        	if(userId == contract.getLessee().getId()) {
                contract.setContractStatus(newStatus);
                rentalContractRepository.save(contract);
        	}
        	/*else {
            	//Ako je raskinuo vlasnik onda korisniku refund proporcionalan broju dana

            	// Prvo izracunati ukupan broj dana ugovora kao i broj preostalih dana
            	LocalDate currDate = LocalDate.now();
            	Long totaldays = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate()) + 1;
            	Long usedDays = ChronoUnit.DAYS.between(contract.getEndDate(), currDate);

            	//Na osnovu toga odrediti cenu po danu i vratiti kolicinu u skladu sa brojem
            	//preostalih dana
            	BigDecimal pricePerDay = contract.getAgreedPrice().divide(BigDecimal.valueOf(totaldays), 2, RoundingMode.HALF_UP);
            	BigDecimal refund = pricePerDay.multiply(BigDecimal.valueOf(totaldays - usedDays));

            	User owner = userRepository.findByIdForUpdate(contract.getAd().getOwner().getId())
            			.orElseThrow(() -> new RuntimeException("User not found!"));

            	User lessee = userRepository.findByIdForUpdate(contract.getLessee().getId())
            			.orElseThrow(() -> new RuntimeException("User not found!"));


            	userRepository.save(lessee);
            	userRepository.save(owner);

                contract.setContractStatus(newStatus);
                rentalContractRepository.save(contract);
        	}*/
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
            List<ContractStatus> activeStatuses = List.of(ContractStatus.ACCEPTED, ContractStatus.ACTIVE);
            List<RentalContract> activeContracts = rentalContractRepository
                    .findActiveContractForAd(contract.getAd().getId(), activeStatuses);

            int available = getAvailableAmountForInterval(
                    activeContracts, req.getNewStartDate(), req.getNewEndDate(),
                    contract.getAd().getTotalQuantity());

            if (contract.getAmount() > available) {
                throw new RuntimeException("Nema dovoljno dostupnih predmeta za tražene datume.");
            }
            contract.setStartDate(req.getNewStartDate());
            contract.setEndDate(req.getNewEndDate());
        }

        if (req.getNewPrice() == null) {
            throw new RuntimeException("Morate ponuditi novu cenu u kontra-ponudi!");
        }
        contract.setAgreedPrice(req.getNewPrice());
    }

	@Override
	public RentalContractDto getRentalContractById(Long rentalId) {
		RentalContract contract = rentalContractRepository.findById(rentalId)
				.orElseThrow(() -> new RuntimeException("No rental contract found!"));
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
	
	public String mapSortField(String frontendField) {
		return switch(frontendField) {
		case "adTitle" -> "ad.title";
		case "firstname" -> "ad.owner.firstname";
		default -> frontendField;
		};
	}

	public boolean isActiveOrAccepted(ContractStatus status) {
		return (status == ContractStatus.ACCEPTED || status == ContractStatus.ACTIVE);
	}
	
	@Transactional
	@Override
	public String delete(Long userId, Long rentalId) {
		
		RentalContract currContr = rentalContractRepository.findById(rentalId)
				.orElseThrow(() -> new RuntimeException("Error deleting contract - contract not found"));
		
		if(currContr.getLessee().getId() != userId) {
			throw new RuntimeException("Deleting someone's contract - not allowed!");
		}
		
		if(isActiveOrAccepted(currContr.getContractStatus())) {
			throw new RuntimeException("Trying to delete ongoing contract - not allowed!");
		}
		
		if(currContr.getContractStatus() != ContractStatus.DELETED) {
			currContr.setContractStatus(ContractStatus.DELETED);
		}else {
			throw new RuntimeException("Contract already deleted!");
		}
		rentalContractRepository.save(currContr);
		
		return "Successfully deleted your contract!";
	}
	
	@Override
	public void markToAdDeleted(Long adId) {
		rentalContractRepository.markToAdDeleted(adId);
		
	}

	@Override
	public RentalContractDto blockDates(CreateRentalContractRequestDto req, Long userId) {
		User owner = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("Error in getting user"));
		Ad ad = adRepository.findById(req.getAdId()).orElseThrow(()-> new RuntimeException("Error in getting ad"));
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

	@Recover 
	public void recover(OptimisticLockException e) {
		throw new RuntimeException("Another operation modified Your account, please try again.");
	}
	
}
