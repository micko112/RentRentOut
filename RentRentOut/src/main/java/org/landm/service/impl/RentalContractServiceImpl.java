package org.landm.service.impl;

import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.exception.UserNotFoundException;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.mapper.RentalContractMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
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

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

@Service
public class RentalContractServiceImpl implements RentalContractService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final RentalContractMapper rentalContractMapper;
    private final RentalContractRepository rentalContractRepository;

    public RentalContractServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, AdRepository adRepository,
                                     RentalContractMapper rentalContractMapper, RentalContractRepository rentalContractRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.rentalContractMapper = rentalContractMapper;
        this.rentalContractRepository = rentalContractRepository;
    }
    
    @Override
    @Retryable(
    		value = OptimisticLockException.class,
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 100)
    		)
    @Transactional
    public RentalContractDto create(CreateRentalContractRequestDto req, long userId) {
    	
        User lessee = userRepository.findByIdForCheck(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Found user");
        
        if (lessee.getMoney().compareTo(req.getAgreedPrice()) >= 0) { //CHANGE TO < 0 
        	//Doesn't allow user without enough money to send offer -> 
        	//Opt_Read because someone can accept earlier users's offers from other ads and spend user's money
        	System.out.println("No enough money");
        	throw new RuntimeException("You don't have enough money for this offer!");
        }
        
        
        
        Ad ad = adRepository.findById(req.getAdId())
                .orElseThrow(() -> new RuntimeException("Ad not found"));

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
        return rentalContractMapper.toDto(rentalContractRepository.save(rentalToCreate));
    }

    @Override
    public List<RentalContract> findByAdIdAndContractStatusIn(long adId, List<ContractStatus> statusList){
    	return rentalContractRepository.findByAdIdAndContractStatusIn(adId, statusList);
    }
    
    @Transactional
    @Override
    public RentalContractDto updateStatus(long contractId, UpdateRentalContractStatusRequestDto req, 
    		long userId) {

        RentalContract contract = rentalContractRepository.findByIdForUpdate(contractId);
        if(contract == null) throw new RuntimeException("Error searching contract!");

        checkPermissions(userId, contract);

        ContractStatus oldStatus = contract.getContractStatus();
        ContractStatus newStatus = req.getNewStatus();

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition");
        }

        updatedAdAvailability(contract, req.getNewStatus());

        handlePriceNegotiation(contract, req, userId);
        contract.setContractStatus(newStatus);
        
        return rentalContractMapper.toDto(contract);
    }

    private static class Event{
    	LocalDate date;
    	long itemCount;
    	
    	Event(LocalDate date, long amount){
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
                            to == ContractStatus.REQUESTED;

            case ACCEPTED ->
                    to == ContractStatus.ACTIVE;

            case ACTIVE ->
                    to == ContractStatus.FINISHED ||
                            to == ContractStatus.CANCELLED;

            default -> false;
        };
    }
    private void checkPermissions(long userId, RentalContract contract){
        boolean isOwner = contract.getAd().getOwner().getId() == userId;
        boolean isLessee = contract.getLessee().getId() == userId;
        if(!isOwner && !isLessee){
            throw new AccessDeniedException("User is not in contract");
        }
    }
    private void updatedAdAvailability(RentalContract contract, ContractStatus newStatus){
        Ad ad = contract.getAd();
        ContractStatus oldStatus = contract.getContractStatus();
        if(oldStatus == ContractStatus.REQUESTED && newStatus == ContractStatus.ACCEPTED){
            if(ad.getAvailableQuantity() <=0){
                throw new RuntimeException("Not available");
            }
            ad.setAvailableQuantity(ad.getAvailableQuantity()-1);
            adRepository.save(ad);
        }
        if (oldStatus == ContractStatus.ACTIVE && (newStatus == ContractStatus.CANCELLED || newStatus == ContractStatus.FINISHED)) {
            ad.setAvailableQuantity(ad.getAvailableQuantity() + 1);
            adRepository.save(ad);
        }

    }

    private void handlePriceNegotiation(RentalContract contract, UpdateRentalContractStatusRequestDto req, long userId){
        ContractStatus oldStatus = contract.getContractStatus();
        ContractStatus newStatus = req.getNewStatus();
        if(newStatus == ContractStatus.REQUESTED && contract.getContractStatus() == ContractStatus.REQUESTED) {
            if(contract.getOfferSender().getId() == userId){
                throw new IllegalStateException("You cannot send counter-offer to yourself");
            }
            contract.setOfferSender(
                    userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found!")));
            if(req.getNewPrice() == null) throw new RuntimeException("New price must be bidded!");
            contract.setAgreedPrice(req.getNewPrice());
        }
    }

	@Override
	public RentalContractDto getRentalContractById(long rentalId) {
		RentalContract contract = rentalContractRepository.findById(rentalId)
				.orElseThrow(() -> new RuntimeException("No rental contract found!"));
		return rentalContractMapper.toDto(contract);
	}

	@Override
	public List<RentalContractDto> getAll(long userId) {
		return rentalContractRepository.findAllByUser(userId)
				.stream()
				.map(rentalContractMapper::toDto)
				.toList();
	}
	
	@Override
	public Page<RentalContractDto> search(long userId, boolean isAdmin, RentalContractSearchDto searchDto) {
		
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
	public String delete(long userId, long rentalId) {
		
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
	public void markToAdDeleted(long adId) {
		rentalContractRepository.markToAdDeleted(adId);
		
	}

	@Recover 
	public void recover(OptimisticLockException e) {
		throw new RuntimeException("Another operation modified Your account, please try again.");
	}
	
}
