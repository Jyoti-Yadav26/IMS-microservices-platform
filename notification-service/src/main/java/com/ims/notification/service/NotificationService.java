package com.ims.notification.service;

import com.ims.notification.entity.Notification;
import com.ims.notification.entity.NotificationType;
import com.ims.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Stands in for an actual email/SMS/push provider call - logs and persists instead. */
    public Notification send(NotificationType type, String recipient, String subject, String message, String referenceId) {
        Notification notification = Notification.builder()
                .type(type)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .referenceId(referenceId)
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Notification sent [{}] to={} subject='{}'", type, recipient, subject);
        return saved;
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
}
