package org.landm.service;

import org.landm.entity.User;
import org.landm.dto.user.UserDto;
import org.landm.entity.EmailVerificationToken;

public interface EmailVerificationService {

	public EmailVerificationToken createAndSaveToken(User user);
	
	public void sendVerificationEmail(String email, String token);
	
	public UserDto verifyEmail(String token);
	
}
