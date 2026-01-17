package org.landm.mapper;


import org.landm.dto.UserDto;
import org.landm.dto.requestDto.user.UpdateUserDto;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user){
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstname());
        userDto.setLastname(user.getLastname());
        userDto.setEmail(user.getEmail());
        userDto.setMoney(user.getMoney());
        return userDto;
    }
    
    public UpdateUserDto toEditDto(User user) {
    	UpdateUserDto editUserDto = new UpdateUserDto();
    	editUserDto.setFirstname(user.getFirstname());
    	editUserDto.setLastname(user.getLastname());
    	editUserDto.setEmail(user.getEmail());
    	return editUserDto;
    }
}
