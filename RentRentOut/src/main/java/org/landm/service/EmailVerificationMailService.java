package org.landm.service;

import org.landm.entity.User;
import org.landm.entity.EmailVerificationToken;

public interface EmailVerificationMailService {

	public EmailVerificationToken createAndSaveToken(User user);
	
	public void sendVerificationEmail(String email, String token);
	
	public void verifyEmail(String token);
	
}
