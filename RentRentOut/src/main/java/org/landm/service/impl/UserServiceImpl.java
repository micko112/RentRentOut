package org.landm.service.impl;

import org.landm.dto.UserDto;
import org.landm.entity.User;
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

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto register(RegisterUserRequestDto req) {
        try {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new Exception("Email already exists!");
            } else {
                userRepository.save(new User(
                        req.getEmail(),
                        passwordEncoder.encode(req.getPassword()),
                        req.getFirstname(),
                        req.getLastname()));
                return new UserDto(req.getFirstname(), req.getLastname(),
                        req.getEmail(), BigDecimal.ZERO);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
