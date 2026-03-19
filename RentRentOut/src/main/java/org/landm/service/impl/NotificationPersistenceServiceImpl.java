package org.landm.service.impl;

import org.landm.dto.notification.NotificationDto;
import org.landm.entity.Enums.NotificationType;
import org.landm.entity.Notification;
import org.landm.entity.User;
import org.landm.repository.NotificationRepository;
import org.landm.repository.UserRepository;
import org.landm.service.NotificationPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationPersistenceServiceImpl implements NotificationPersistenceService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationPersistenceServiceImpl(NotificationRepository notificationRepository,
                                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void create(Long recipientId, NotificationType type, String title, String message,
                       Long relatedEntityId, String relatedEntityType, String actorName) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("User not found: " + recipientId));

        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedEntityId(relatedEntityId);
        n.setRelatedEntityType(relatedEntityType);
        n.setActorName(actorName);
        n.setRead(false);

        notificationRepository.save(n);
    }

    @Override
    public List<NotificationDto> getForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markOneAsRead(Long notificationId, Long userId) {
        notificationRepository.markOneAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setType(n.getType().name());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setRelatedEntityId(n.getRelatedEntityId());
        dto.setRelatedEntityType(n.getRelatedEntityType());
        dto.setActorName(n.getActorName());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
