package org.landm.mapper;


import org.landm.dto.UserDto;
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
}
