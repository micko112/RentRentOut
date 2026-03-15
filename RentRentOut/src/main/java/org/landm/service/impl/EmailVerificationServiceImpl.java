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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class EmailVerificationServiceImpl implements  EmailVerificationService{
	
	@Value("${app.frontend.url:http://localhost:4200/verify-email}")
	private String frontendUrl;
	private EmailVerificationRepository tokenRepository;
	private UserRepository userRepository;
	private final JavaMailSender javaMailSender;
	private final UserMapper userMapper;
	
	public EmailVerificationServiceImpl(EmailVerificationRepository tokenRepo, 
			UserRepository userRepository, JavaMailSender javaMailSender, 
			UserMapper userMapper) {
		this.javaMailSender = javaMailSender;
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
	public void sendVerificationEmail(String email, String token) {
		
		String link = frontendUrl + "?token=" + token;
		
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(email);
		msg.setFrom("rentrentout@gmail.com");
		msg.setSubject("Please confirm Your e-mail address");
		msg.setText("Use link provided below to confirm Your e-mail address.\n\n"
		+ link + "\n\nThank You for choosing us! \n\nSincerely, \nRentRentOut team");
		
		javaMailSender.send(msg);
		
	}

	@Transactional
	@Override
	public UserDto verifyEmail(String token) {
		
		EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Token is not valid.")); 
		
		if(verificationToken.isUsed()) {
			throw new RuntimeException("Token already used.");
		}
		
		if(verificationToken.getExpiresAt().isBefore(LocalDate.now())) {
			throw new RuntimeException("Token is expired.");
		}
		
		verificationToken.setUsed(true);
		tokenRepository.save(verificationToken);
		
		User user = verificationToken.getUser();
		user.setEnabled(true);
		
		userRepository.save(user);
		
		return userMapper.toDto(user);
		
	}
	
	

		
}
