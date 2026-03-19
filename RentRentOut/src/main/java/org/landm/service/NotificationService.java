package org.landm.service;

import org.landm.entity.Ad;
import org.landm.entity.User;

public interface NotificationService {
    void sendContractRequestEmail(User owner, Ad ad, User lessee);
    void sendContractAcceptedEmail(User lessee, Ad ad);
    void sendContractRejectedEmail(User lessee, Ad ad);

    void savePushSubscription(String endpoint, String p256dh, String auth, Long userId);
    void deletePushSubscription(String endpoint, Long userId);
    void sendPushNotification(Long userId, String title, String body);
}
