package com.ims.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A record of every notification "sent" (in this demo we just log + persist rather than
 * calling a real email/SMS provider). Keeping history here - rather than only logging -
 * means notification-service has something meaningful to expose over its own REST API
 * and is independently useful/verifiable, e.g. from a support-agent tool.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String message;

    /** orderNumber for order events, sku for low-stock events - kept generic and untyped on purpose. */
    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
