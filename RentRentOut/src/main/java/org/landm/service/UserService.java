package org.landm.service;

import org.landm.entity.User;
import org.landm.dto.user.UserDto;
import org.landm.dto.requestDto.DepositRequestDto;
import org.landm.dto.user.ChangeUserPasswordDto;
import org.landm.dto.user.LoginUserRequestDto;
import org.landm.dto.user.RegisterUserRequestDto;
import org.landm.dto.user.UpdateUserDto;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public User login(LoginUserRequestDto req);
    
    public UserDto get(long userId);
    
    public UserDto getMe(long userId);
    
    public UserDto depositMoney(long userId, DepositRequestDto req);
    
    public UpdateUserDto update(UpdateUserDto editUserDto, long userId);
    
    public String updatePassword(ChangeUserPasswordDto data, long userId);
    
    public String deleteMe(long myId);
}
