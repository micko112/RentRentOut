package org.landm.service;

import org.landm.entity.User;
import org.landm.entity.EmailVerificationToken;

public interface EmailVerificationService {

	public EmailVerificationToken createAndSaveToken(User user);

	public void sendVerificationEmail(String email, String firstname, String token);

	public User verifyEmail(String token);

}
