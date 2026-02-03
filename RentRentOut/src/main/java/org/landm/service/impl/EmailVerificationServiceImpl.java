package org.landm.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.landm.entity.EmailVerificationToken;
import org.landm.entity.User;
import org.landm.repository.EmailVerificationTokenRepository;
import org.landm.repository.UserRepository;
import org.landm.service.EmailVerificationMailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class EmailVerificationServiceImpl implements  EmailVerificationMailService{
	
//	@Value("${app.frontend.url}")
	private String frontendUrl = "/api/user";
	private EmailVerificationTokenRepository tokenRepository;
	private UserRepository userRepository;
//	private final JavaMailSender javaMailSender;
	
	public EmailVerificationServiceImpl(EmailVerificationTokenRepository tokenRepo, 
			UserRepository userRepository) {
//		this.javaMailSender = javaMailSender;
		this.tokenRepository = tokenRepo;
		this.userRepository = userRepository;
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
		
		String link = frontendUrl + "/validate-email?token=" + token;
		
//		SimpleMailMessage msg = new SimpleMailMessage();
//		msg.setTo(email);
//		msg.setFrom("kreiraniEmail");
//		msg.setSubject("Please confirm Your e-mail address");
//		msg.setText("Use link provided below to confirm Your e-mail address. \n" + link);
//		
//		javaMailSender.send(msg);
		
	}

	@Transactional
	@Override
	public void verifyEmail(String token) {
		
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
		
	}
	
	

		
}
