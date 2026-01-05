package org.landm.mapper;

import org.landm.dto.UserDto;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user){
        return new UserDto(
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getMoney()
        );
    }
}
