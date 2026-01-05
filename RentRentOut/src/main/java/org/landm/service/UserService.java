package org.landm.service;

import org.landm.dto.RegisterUserRequestDto;
import org.landm.dto.UserDto;

public interface UserService {

    public UserDto register(RegisterUserRequestDto req);
}
