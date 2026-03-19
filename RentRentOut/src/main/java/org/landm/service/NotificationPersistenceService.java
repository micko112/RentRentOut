package org.landm.service;

import org.landm.dto.notification.NotificationDto;
import org.landm.entity.Enums.NotificationType;

import java.util.List;

public interface NotificationPersistenceService {

    void create(Long recipientId, NotificationType type, String title, String message,
                Long relatedEntityId, String relatedEntityType, String actorName);

    List<NotificationDto> getForUser(Long userId);

    long getUnreadCount(Long userId);

    void markOneAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
