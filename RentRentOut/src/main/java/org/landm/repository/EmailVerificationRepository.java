package org.landm.repository;

import java.util.Optional;

import org.landm.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationToken, Long> {

	public Optional<EmailVerificationToken> findByToken(String token);
	
}
