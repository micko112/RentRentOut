package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.LoginUserRequestDto;
import org.landm.dto.RegisterUserRequestDto;
import org.landm.dto.UserDto;
import org.landm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser
            (@Valid @RequestBody RegisterUserRequestDto req){
        return new ResponseEntity<>(userService.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginUserRequestDto req){
        return new ResponseEntity<>(userService.login(req), HttpStatus.ACCEPTED);
    }



}
