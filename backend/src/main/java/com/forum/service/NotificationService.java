package com.forum.service;

import com.forum.dto.NotificationDto;
import com.forum.model.Notification;
import com.forum.model.User;
import com.forum.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(User recipient, String message, String type, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        
        Notification saved = notificationRepository.save(notification);
        
        // Convert to DTO
        NotificationDto dto = new NotificationDto(
            saved.getId(),
            saved.getMessage(),
            saved.getType(),
            saved.getRelatedEntityId(),
            saved.isRead(),
            saved.getTimestamp()
        );

        // Send via WebSocket
        // Destination: /topic/notifications/{userId}
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
    }

    public List<NotificationDto> getUserNotifications(User user) {
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(user.getId())
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getMessage(),
                        n.getType(),
                        n.getRelatedEntityId(),
                        n.isRead(),
                        n.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
