package org.landm.service;

import org.landm.entity.User;
import org.landm.dto.UserDto;
import org.landm.dto.requestDto.user.ChangeUserPasswordDto;
import org.landm.dto.requestDto.user.LoginUserRequestDto;
import org.landm.dto.requestDto.user.RegisterUserRequestDto;
import org.landm.dto.requestDto.user.UpdateUserDto;

import java.util.Map;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public User login(LoginUserRequestDto req);
    
    public UpdateUserDto getMe(long userId); 
    
    public UpdateUserDto updateMe(UpdateUserDto editUserDto, long userId);
    
    public String updatePassword(ChangeUserPasswordDto data, long userId);
}
