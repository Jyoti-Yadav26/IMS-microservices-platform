package com.ims.notification.dto;

import com.ims.notification.entity.Notification;
import com.ims.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String recipient,
        String subject,
        String message,
        String referenceId,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getRecipient(), n.getSubject(), n.getMessage(), n.getReferenceId(), n.getCreatedAt());
    }
}
