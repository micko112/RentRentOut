package org.landm.controller;

import org.landm.dto.user.UserDto;
import org.landm.service.EmailVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;

	public EmailVerificationController(EmailVerificationService emailVerificationService) {
		this.emailVerificationService = emailVerificationService;
	}
	
	@GetMapping("/validate-email")
	public ResponseEntity<UserDto> validateEmail(@RequestParam("token") String token){
		return new ResponseEntity<>(emailVerificationService.verifyEmail(token), HttpStatus.OK);
	}
	
	
	
}
