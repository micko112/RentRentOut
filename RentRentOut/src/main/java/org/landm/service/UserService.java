package org.landm.service;

import org.landm.dto.LoginUserRequestDto;
import org.landm.dto.RegisterUserRequestDto;
import org.landm.dto.UserDto;

import java.util.Map;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);

    public Map<String, Object> login(LoginUserRequestDto req);
}
