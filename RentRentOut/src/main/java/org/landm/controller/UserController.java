package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.requestDto.DepositRequestDto;
import org.landm.dto.user.*;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/{id}")
    public ResponseEntity<PublicProfileDto> getUser(@PathVariable("id") Long userId, Pageable pageable){

    	return ResponseEntity.ok(userService.getUser(pageable, userId));
    }
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable("id") Long userId){
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication auth){
    	Long myId = Long.parseLong(auth.getName());
    	return ResponseEntity.ok(userService.getMe(myId));
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
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/me/deposits")
    public ResponseEntity<UserDto> depositMoney(Authentication auth, @RequestBody @Valid DepositRequestDto req){
    
    	Long userId = Long.parseLong(auth.getName());
    	
    	return new ResponseEntity<>(userService.depositMoney(userId, req), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(@Valid @RequestBody UpdateUserDto userInfo, Authentication auth){
    	Long myId = Long.parseLong(auth.getName());
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.update(userInfo, myId));
    	return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/me/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody ChangeUserPasswordDto data, Authentication auth){
    	Long myId = Long.parseLong(auth.getName());
		return new ResponseEntity<>(userService.updatePassword(data, myId), HttpStatus.OK);	
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody UpdateUserDto userInfo, @PathVariable("id") Long userId){
    	return new ResponseEntity<>(userService.update(userInfo, userId), HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(Authentication auth) {
        Long myId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(userService.deleteMe(myId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}/phone")
    public ResponseEntity<Map<String, String>> getUserPhone(@PathVariable("id") Long userId) {
        String phone = userService.getRealPhoneNumber(userId);
        Map<String, String> response = new HashMap<>();
        if (phone != null && !phone.isBlank()) {
            response.put("phone", phone);
        } else {
            response.put("phone", "Korisnik nema telefon");
        }

        return ResponseEntity.ok(response);
    }
}
