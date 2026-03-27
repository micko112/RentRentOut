package org.landm.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.landm.dto.user.UserDto;
import org.landm.entity.EmailVerificationToken;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.repository.EmailVerificationRepository;
import org.landm.repository.UserRepository;
import org.landm.service.EmailVerificationService;
import org.landm.service.HtmlEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

	private final EmailVerificationRepository tokenRepository;
	private final UserRepository userRepository;
	private final HtmlEmailService htmlEmailService;
	private final UserMapper userMapper;

	@Value("${app.frontend.base-url:http://localhost:4200}")
	private String frontendBaseUrl;

	public EmailVerificationServiceImpl(EmailVerificationRepository tokenRepo,
			UserRepository userRepository, HtmlEmailService htmlEmailService,
			UserMapper userMapper) {
		this.htmlEmailService = htmlEmailService;
		this.tokenRepository = tokenRepo;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}
	
	@Override
	public EmailVerificationToken createAndSaveToken(User user) {
		EmailVerificationToken verificationToken = new EmailVerificationToken();
		String token = UUID.randomUUID().toString();
		
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		verificationToken.setExpiresAt(LocalDate.now().plusDays(1));
		
		tokenRepository.save(verificationToken);
		
		return verificationToken;
	}

	@Override
	public void sendVerificationEmail(String email, String firstname, String token) {
		String link = frontendBaseUrl + "/verify-email?token=" + token;
		htmlEmailService.sendVerificationEmail(email, firstname, link);
	}

	@Transactional
	@Override
	public UserDto verifyEmail(String token) {
		
		EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("Token is not valid."));
		
		if(verificationToken.isUsed()) {
			throw new IllegalStateException("Token already used.");
		}

		if(verificationToken.getExpiresAt().isBefore(LocalDate.now())) {
			throw new IllegalStateException("Token is expired.");
		}
		
		verificationToken.setUsed(true);
		tokenRepository.save(verificationToken);
		
		User user = verificationToken.getUser();
		user.setEnabled(true);
		
		userRepository.save(user);
		
		return userMapper.toDto(user);
	}
}
