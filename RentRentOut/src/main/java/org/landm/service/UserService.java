package org.landm.service;

import org.landm.dto.requestDto.LoginUserRequestDto;
import org.landm.dto.requestDto.RegisterUserRequestDto;
import org.landm.entity.User;
import org.landm.dto.UserDto;

import java.util.Map;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public User login(LoginUserRequestDto req);

    public UserDto update(UserDto newInfo, String authHeader);
}
