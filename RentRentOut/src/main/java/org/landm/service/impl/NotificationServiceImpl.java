package org.landm.service.impl;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.landm.entity.Ad;
import org.landm.entity.PushSubscription;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.repository.PushSubscriptionRepository;
import org.landm.repository.UserRepository;
import org.landm.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String FROM_EMAIL = "rentrentout@gmail.com";

    private final JavaMailSender mailSender;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;
    private final PushService pushService;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public NotificationServiceImpl(
            JavaMailSender mailSender,
            PushSubscriptionRepository pushSubscriptionRepository,
            UserRepository userRepository,
            @Value("${app.vapid.public-key}") String vapidPublicKey,
            @Value("${app.vapid.private-key}") String vapidPrivateKey,
            @Value("${app.vapid.subject}") String vapidSubject) throws Exception {
        this.mailSender = mailSender;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userRepository = userRepository;

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
    }

    @Override
    public void sendContractRequestEmail(User owner, Ad ad, User lessee) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(owner.getEmail());
            msg.setFrom(FROM_EMAIL);
            msg.setSubject("New rental request for: " + ad.getTitle());
            msg.setText(
                "Hello " + owner.getFirstname() + ",\n\n" +
                lessee.getFirstname() + " " + lessee.getLastname() + " wants to rent your item: " + ad.getTitle() + ".\n\n" +
                "Log in to review the request: " + frontendBaseUrl + "/user/contracts\n\n" +
                "Sincerely,\nRentRentOut team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send contract request email: {}", e.getMessage());
        }

        sendPushNotification(
            owner.getId(),
            "New rental request",
            lessee.getFirstname() + " wants to rent: " + ad.getTitle()
        );
    }

    @Override
    public void sendContractAcceptedEmail(User lessee, Ad ad) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(lessee.getEmail());
            msg.setFrom(FROM_EMAIL);
            msg.setSubject("Your rental request was accepted: " + ad.getTitle());
            msg.setText(
                "Hello " + lessee.getFirstname() + ",\n\n" +
                "Great news! Your rental request for \"" + ad.getTitle() + "\" has been accepted.\n\n" +
                "View your contracts: " + frontendBaseUrl + "/user/contracts\n\n" +
                "Sincerely,\nRentRentOut team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send contract accepted email: {}", e.getMessage());
        }

        sendPushNotification(
            lessee.getId(),
            "Rental request accepted!",
            "Your request for \"" + ad.getTitle() + "\" was accepted."
        );
    }

    @Override
    public void sendContractRejectedEmail(User lessee, Ad ad) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(lessee.getEmail());
            msg.setFrom(FROM_EMAIL);
            msg.setSubject("Your rental request was declined: " + ad.getTitle());
            msg.setText(
                "Hello " + lessee.getFirstname() + ",\n\n" +
                "Unfortunately, your rental request for \"" + ad.getTitle() + "\" has been declined.\n\n" +
                "Browse other listings: " + frontendBaseUrl + "\n\n" +
                "Sincerely,\nRentRentOut team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send contract rejected email: {}", e.getMessage());
        }

        sendPushNotification(
            lessee.getId(),
            "Rental request declined",
            "Your request for \"" + ad.getTitle() + "\" was declined."
        );
    }

    @Override
    @Transactional
    public void savePushSubscription(String endpoint, String p256dh, String auth, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Briše postojeću pretplatu za isti endpoint (upsert — nema duplikata)
        pushSubscriptionRepository.deleteByEndpointAndUserId(endpoint, userId);

        PushSubscription sub = new PushSubscription();
        sub.setUser(user);
        sub.setEndpoint(endpoint);
        sub.setP256dh(p256dh);
        sub.setAuth(auth);
        pushSubscriptionRepository.save(sub);
    }

    @Override
    @Transactional
    public void deletePushSubscription(String endpoint, Long userId) {
        pushSubscriptionRepository.deleteByEndpointAndUserId(endpoint, userId);
    }

    @Override
    public void sendPushNotification(Long userId, String title, String body) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserId(userId);
        String payload = "{\"title\":\"" + escapeJson(title) + "\",\"body\":\"" + escapeJson(body) + "\"}";

        for (PushSubscription sub : subscriptions) {
            try {
                Subscription subscription = new Subscription(
                    sub.getEndpoint(),
                    new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                );
                Notification notification = new Notification(subscription, payload);
                pushService.send(notification);
            } catch (Exception e) {
                log.warn("Failed to send push notification to endpoint {}: {}", sub.getEndpoint(), e.getMessage());
            }
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
