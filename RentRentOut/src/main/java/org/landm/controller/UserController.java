package org.landm.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.landm.dto.requestDto.DepositRequestDto;
import org.landm.dto.user.*;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    public UserController(UserService userService, JwtUtil jwtUtil,
    		UserMapper userMapper) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    private void setAuthCookies(HttpServletResponse response, User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        response.addHeader("Set-Cookie", buildCookie("access_token",  accessToken,  accessExpiration  / 1000).toString());
        response.addHeader("Set-Cookie", buildCookie("refresh_token", refreshToken, refreshExpiration / 1000).toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
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
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginUserRequestDto req, HttpServletResponse response) {
        User user = userService.login(req);
        setAuthCookies(response, user);
        Map<String, Object> res = new HashMap<>();
        res.put("user", userMapper.toDto(user));
        res.put("wsToken", jwtUtil.generateToken(user));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) return ResponseEntity.badRequest().build();
        User user = userService.googleLogin(idToken);
        setAuthCookies(response, user);
        Map<String, Object> res = new HashMap<>();
        res.put("user", userMapper.toDto(user));
        res.put("wsToken", jwtUtil.generateToken(user));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/facebook-login")
    public ResponseEntity<Map<String, Object>> facebookLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String accessToken = body.get("accessToken");
        if (accessToken == null || accessToken.isBlank()) return ResponseEntity.badRequest().build();
        User user = userService.facebookLogin(accessToken);
        setAuthCookies(response, user);
        Map<String, Object> res = new HashMap<>();
        res.put("user", userMapper.toDto(user));
        res.put("wsToken", jwtUtil.generateToken(user));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/apple-login")
    public ResponseEntity<Map<String, Object>> appleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String identityToken = body.get("identityToken");
        if (identityToken == null || identityToken.isBlank()) return ResponseEntity.badRequest().build();
        User user = userService.appleLogin(identityToken);
        setAuthCookies(response, user);
        Map<String, Object> res = new HashMap<>();
        res.put("user", userMapper.toDto(user));
        res.put("wsToken", jwtUtil.generateToken(user));
        return new ResponseEntity<>(res, HttpStatus.OK);
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
    	userService.update(userInfo, myId);
    	Map<String, Object> res = new HashMap<>();
    	res.put("user", userService.getMe(myId));
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
        response.put("phone", (phone != null && !phone.isBlank()) ? phone : null);
        return ResponseEntity.ok(response);
    }
}
