package org.landm.mapper;


import org.landm.dto.user.UserDto;
import org.landm.dto.user.UpdateUserDto;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user){
        UserDto userDto = new UserDto();
        userDto.setFirstname(user.getFirstname());
        userDto.setLastname(user.getLastname());
        userDto.setEmail(user.getEmail());
        userDto.setMoney(user.getMoney());
        userDto.setCurrency(user.getCurrency().toString());
        userDto.setPositiveReviews(user.getPositiveReviews());
        userDto.setNegativeReviews(user.getNegativeReviews());
        return userDto;
    }
    
    public UpdateUserDto toEditDto(User user) {
    	UpdateUserDto editUserDto = new UpdateUserDto();
    	editUserDto.setFirstname(user.getFirstname());
    	editUserDto.setLastname(user.getLastname());
    	editUserDto.setEmail(user.getEmail());
        editUserDto.setCurrency(user.getCurrency().toString());
    	return editUserDto;
    }
}
