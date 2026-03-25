package org.landm.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.landm.entity.PasswordResetToken;
import org.landm.entity.User;
import org.landm.repository.PasswordResetTokenRepository;
import org.landm.repository.UserRepository;
import org.landm.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${app.reset-password.url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    JavaMailSender mailSender,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.isEnabled()) {
            // Silently ignore to prevent user enumeration
            return;
        }

        tokenRepository.deleteAllByUserId(user.getId());

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDate.now().plusDays(1));
        tokenRepository.save(resetToken);

        String link = resetPasswordUrl + "?token=" + resetToken.getToken();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Resetovanje lozinke");
        msg.setText("Koristite link ispod da resetujete vašu lozinku.\n\n"
                + link + "\n\nLink važi do kraja sutrašnjeg dana.\n\nSrdačno,\nIzdajemiZnajmljujem tim");

        mailSender.send(msg);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token is not valid."));

        if (resetToken.isUsed()) {
            throw new IllegalStateException("Token already used.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Token is expired.");
        }

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
