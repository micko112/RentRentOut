package org.landm.service.impl;

import org.landm.dto.rentalContract.RentalContractDto;

import java.util.ArrayList;
import java.util.List;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.mapper.RentalContractMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.RentalContractService;
import org.springframework.stereotype.Service;

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
    public RentalContractDto create(CreateRentalContractRequestDto req, String token) {
        long lesseeId = jwtUtil.extractUserId(token);
        User lessee = userRepository.findById(lesseeId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Ad ad = adRepository.findById(req.getAdId())
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        RentalContract rentalToCreate = rentalContractMapper.toEntity(req);
        rentalToCreate.setAd(ad);
        rentalToCreate.setLessee(lessee);

        return rentalContractMapper.toDto(rentalContractRepository.save(rentalToCreate));
    }

    @Override
    public RentalContractDto updateStatus(long contractId, UpdateRentalContractStatusRequestDto req, HEAD String) {
        return null;
    }

    @Override
    public RentalContractDto updateStatus(long contractId, UpdateRentalContractStatusRequestDto req, long userId) {

        RentalContract contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        Ad ad = contract.getAd();

        if (ad.getOwner().getId() != (userId)) {
            throw new RuntimeException("Not allowed");
        }
        ContractStatus oldStatus = contract.getContractStatus();
        ContractStatus newStatus = req.getNewStatus();

        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition");
        }
        if (oldStatus == ContractStatus.REQUESTED &&
                newStatus == ContractStatus.ACCEPTED) {

            if (ad.getAvailableQuantity() <= 0) {
                throw new RuntimeException("No available quantity");
            }
            ad.setAvailableQuantity(ad.getAvailableQuantity() - 1);
        }
        if (oldStatus == ContractStatus.ACTIVE &&
                (newStatus == ContractStatus.CANCELLED ||
                        newStatus == ContractStatus.FINISHED)) {

            ad.setAvailableQuantity(ad.getAvailableQuantity() + 1);
        }
        contract.setContractStatus(newStatus);

        return rentalContractMapper.toDto(contract);
    }



    private boolean isValidTransition(ContractStatus from, ContractStatus to) {

        return switch (from) {
            case REQUESTED ->
                    to == ContractStatus.ACCEPTED ||
                            to == ContractStatus.REJECTED;

            case ACCEPTED ->
                    to == ContractStatus.ACTIVE;

            case ACTIVE ->
                    to == ContractStatus.FINISHED ||
                            to == ContractStatus.CANCELLED;

            default -> false;
        };
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
	
	
}
