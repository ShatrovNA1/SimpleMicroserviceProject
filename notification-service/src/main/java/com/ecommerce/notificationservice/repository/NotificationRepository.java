package com.ecommerce.notificationservice.repository;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationChannel;
import com.ecommerce.notificationservice.entity.NotificationStatus;
import com.ecommerce.notificationservice.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByChannel(NotificationChannel channel, Pageable pageable);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    long countByStatus(NotificationStatus status);

    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

