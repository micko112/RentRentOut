package org.landm.service.impl;

import org.landm.dto.LoginUserRequestDto;
import org.landm.dto.UserDto;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.exception.WrongCredentialsException;
import org.landm.mapper.UserMapper;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.landm.dto.RegisterUserRequestDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder, UserMapper userMapper,
                           JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDto register(RegisterUserRequestDto req) {

            if (userRepository.existsByEmail(req.getEmail())) {
                throw new WrongCredentialsException("Email already exists!");
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

    public Map<String, Object> login(LoginUserRequestDto req) {

        User user = userRepository.findByEmail(req.getEmail());

        if (user != null) {
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                throw new WrongCredentialsException("Wrong email or password!");
            }
            Map<String, Object> respMap = new HashMap<>();
            respMap.put("token", jwtUtil.generateToken(user.getUserId()));
            respMap.put("user", userMapper.toDto(user));
            return respMap;
        } else {
            throw new UserNotFoundException("User not found!");
        }
    }
    
    public UserDto update(UserDto newInfo, String authHeader){
    	long userId = jwtUtil.extractUserId(authHeader.substring(7));
    	Optional<User> userToUpdateOpt = userRepository.findById(userId);
    	if(userToUpdateOpt.isPresent()) {
    		User userToUpdate = userToUpdateOpt.get();
    		userToUpdate.setFirstname(newInfo.getFirstName());
    		userToUpdate.setLastname(newInfo.getLastname());
    		userToUpdate = userRepository.save(userToUpdate);
    		return userMapper.toDto(userToUpdate);
    	}else {
    		throw new UserNotFoundException("Error with updating user data!");
    	}
    }
}
