package com.ecommerce.notificationservice.scheduler;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationStatus;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    /**
     * Повторная отправка неудачных уведомлений каждые 5 минут
     */
    @Scheduled(fixedRateString = "${notification.retry.interval:300000}")
    public void retryFailedNotifications() {
        log.debug("Starting retry of failed notifications...");

        List<Notification> failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, maxRetryAttempts);

        if (failedNotifications.isEmpty()) {
            log.debug("No failed notifications to retry");
            return;
        }

        log.info("Found {} failed notifications to retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                log.info("Retrying notification id={}, attempt={}",
                        notification.getId(), notification.getRetryCount() + 1);
                notificationService.retryNotification(notification.getId());
            } catch (Exception e) {
                log.error("Failed to retry notification id={}", notification.getId(), e);
            }
        }
    }
}

