package org.landm.service;

import org.landm.dto.requestDto.LoginUserRequestDto;
import org.landm.dto.requestDto.RegisterUserRequestDto;
import org.landm.dto.UserDto;

import java.util.Map;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public Map<String, Object> login(LoginUserRequestDto req);

    public UserDto update(UserDto newInfo, String authHeader);
}
