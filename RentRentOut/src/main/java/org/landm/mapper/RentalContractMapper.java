package org.landm.mapper;

import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.entity.RentalContract;
import org.springframework.stereotype.Component;

@Component
public class RentalContractMapper {
    public final AdMapper adMapper;
    public final UserMapper userMapper;

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
        dto.setLesseeDto(userMapper.toDto(rc.getLessee()));
        dto.setStartDate(rc.getStartDate());
        dto.setEndDate(rc.getEndDate());
        dto.setAgreedPrice((rc.getAgreedPrice()));
        dto.setCurrency(rc.getCurrency().toString());
        dto.setContractStatus(rc.getContractStatus());
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
