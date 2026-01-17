package org.landm.controller;

import jakarta.validation.Valid;

import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.dto.UserDto;
import org.landm.dto.requestDto.user.ChangeUserPasswordDto;
import org.landm.dto.requestDto.user.LoginUserRequestDto;
import org.landm.dto.requestDto.user.RegisterUserRequestDto;
import org.landm.dto.requestDto.user.UpdateUserDto;
import org.landm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.core.status.Status;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserController(UserService userService, JwtUtil jwtUtil, 
    		UserMapper userMapper) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser
            (@Valid @RequestBody RegisterUserRequestDto req){
        return new ResponseEntity<>(userService.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginUserRequestDto req){
    	User user = userService.login(req);
    	String token = jwtUtil.generateToken(user);
    	
    	Map<String, Object> response = new HashMap<>();
        response.put("user", userMapper.toDto(user));
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(Authentication auth){
    	long myId = Long.parseLong(auth.getName());
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.getMe(myId));
    	return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(@Valid @RequestBody UpdateUserDto userInfo, Authentication auth){
    	long myId = Long.parseLong(auth.getName());
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.updateMe(userInfo, myId));
    	return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    @PatchMapping("/me/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody ChangeUserPasswordDto data, Authentication auth){
    	long myId = Long.parseLong(auth.getName());
		return new ResponseEntity<>(userService.updatePassword(data, myId), HttpStatus.OK);	
    }
    
}
