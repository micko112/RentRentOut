package org.landm.service.impl;

import org.landm.dto.UserDto;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.repository.UserRepository;
import org.landm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.landm.dto.RegisterUserRequestDto;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder, UserMapper userMapper){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto register(RegisterUserRequestDto req) {

            if (userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("Email already exists!");
            } else {
                User userToSave = new User(
                        req.getEmail(),
                        passwordEncoder.encode(req.getPassword()),
                        req.getFirstname(),
                        req.getLastname());

                User savedUser = userRepository.save(userToSave);
                return userMapper.toDto(savedUser);
                //return new UserDto(req.getFirstname(), req.getLastname(),
                  //      req.getEmail(), BigDecimal.ZERO);
            }
    }
}
