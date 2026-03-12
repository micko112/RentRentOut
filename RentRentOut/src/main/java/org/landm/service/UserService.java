package org.landm.service;

import org.landm.dto.requestDto.DepositRequestDto;
import org.landm.dto.user.*;
import org.landm.entity.User;
import org.springframework.data.domain.Pageable;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public User login(LoginUserRequestDto req);
    
    public UserProfileDto getUserProfile(Long userId);
    
    public UserDto getMe(Long userId);
    
    public UserDto depositMoney(Long userId, DepositRequestDto req);
    
    public UpdateUserDto update(UpdateUserDto editUserDto, Long userId);
    
    public String updatePassword(ChangeUserPasswordDto data, Long userId);
    
    public String deleteMe(Long myId);

    public PublicProfileDto getUser(Pageable pageable, Long userId);
}
