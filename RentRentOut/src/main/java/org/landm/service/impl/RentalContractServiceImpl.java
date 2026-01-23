package org.landm.service.impl;

import org.landm.dto.rentalContract.RentalContractDto;

import java.util.ArrayList;
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
import org.landm.service.RentalContractService;
import org.landm.specification.RentalContractSpecification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
    public RentalContractDto create(CreateRentalContractRequestDto req, long userId) {

        User lessee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Ad ad = adRepository.findById(req.getAdId())
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        RentalContract rentalToCreate = rentalContractMapper.toEntity(req);
        rentalToCreate.setAd(ad);
        rentalToCreate.setLessee(lessee);
        rentalToCreate.setOfferSender(lessee);
        rentalToCreate.setContractStatus(ContractStatus.REQUESTED);
        return rentalContractMapper.toDto(rentalContractRepository.save(rentalToCreate));
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
	public List<RentalContractDto> search(String term, long userId, boolean isAdmin) {
		
		List contracts = new ArrayList<>();
		
		return rentalContractRepository
				.findAll(RentalContractSpecification.search(term, userId, isAdmin))
				.stream()
				.map(rentalContractMapper::toDto)
				.toList();
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

	
	
}
