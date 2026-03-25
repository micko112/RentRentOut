package org.landm.mapper;

import org.landm.dto.rentalContract.ContractParticipantDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RentalContractMapper {
    private final AdMapper adMapper;
    private final UserMapper userMapper;

    public RentalContractMapper(AdMapper adMapper, UserMapper userMapper) {
        this.adMapper = adMapper;
        this.userMapper = userMapper;
    }

    public RentalContractDto toDto(RentalContract rc){
        if(rc==null){
            return null;
        }
        RentalContractDto dto = new RentalContractDto();
        dto.setId(rc.getId());
        dto.setAdDto(adMapper.toDto(rc.getAd()));
        dto.setLesseeDto(toParticipantDto(rc.getLessee()));
        dto.setStartDate(rc.getStartDate());
        dto.setEndDate(rc.getEndDate());
        dto.setAgreedPrice((rc.getAgreedPrice()));
        dto.setCurrency(rc.getCurrency().toString());
        dto.setContractStatus(rc.getContractStatus());
        return dto;
    }
    private ContractParticipantDto toParticipantDto(User user) {
        ContractParticipantDto dto = new ContractParticipantDto();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    public RentalContract toEntity(CreateRentalContractRequestDto dto){
        if (dto == null) {
            return null;
        }
        RentalContract rc = new RentalContract();
        rc.setStartDate(dto.getStartDate());
        rc.setEndDate(dto.getEndDate());
        rc.setAgreedPrice(dto.getAgreedPrice());
        rc.setCurrency(dto.getCurrency());
        return rc;
    }
}
