package org.landm.controller;

import jakarta.validation.Valid;

import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.dto.user.UserDto;
import org.landm.dto.user.ChangeUserPasswordDto;
import org.landm.dto.user.LoginUserRequestDto;
import org.landm.dto.user.RegisterUserRequestDto;
import org.landm.dto.user.UpdateUserDto;
import org.landm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable("id") long userId){
    	return new ResponseEntity<>(userService.get(userId), HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/my-profile")
    public ResponseEntity<Map<String, Object>> getMe(Authentication auth){
    	long myId = Long.parseLong(auth.getName());
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.getMe(myId));
    	return new ResponseEntity<>(res, HttpStatus.OK);
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

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/my-profile")
    public ResponseEntity<Map<String, Object>> updateMe(@Valid @RequestBody UpdateUserDto userInfo, Authentication auth){
    	long myId = Long.parseLong(auth.getName());
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.update(userInfo, myId));
    	return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping("/my-profile/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody ChangeUserPasswordDto data, Authentication auth){
    	long myId = Long.parseLong(auth.getName());
		return new ResponseEntity<>(userService.updatePassword(data, myId), HttpStatus.OK);	
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody UpdateUserDto userInfo, @PathVariable("id") long userId){
    	return new ResponseEntity<>(userService.update(userInfo, userId), HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/my-profile")
    public ResponseEntity<String> deleteMe(Authentication auth) {
        long myId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(userService.deleteMe(myId), HttpStatus.OK);
    }
}
