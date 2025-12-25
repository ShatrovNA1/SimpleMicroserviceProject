package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.dto.SendNotificationRequest;
import com.ecommerce.notificationservice.entity.NotificationChannel;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.event.UserEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.user-events:user-events}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void consumeUserEvent(UserEvent event) {
        log.info("Received user event: type={}, userId={}", event.getEventType(), event.getUserId());

        try {
            NotificationType notificationType = mapEventTypeToNotificationType(event.getEventType());

            if (notificationType != null && event.getEmail() != null) {
                String subject = getSubject(notificationType, event);
                String content = generateContent(notificationType, event);

                SendNotificationRequest request = SendNotificationRequest.builder()
                        .userId(event.getUserId())
                        .recipient(event.getEmail())
                        .type(notificationType)
                        .channel(NotificationChannel.EMAIL)
                        .subject(subject)
                        .content(content)
                        .referenceId(String.valueOf(event.getUserId()))
                        .referenceType("USER")
                        .build();

                notificationService.sendNotification(request);
                log.info("User notification sent for userId={}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing user event: userId={}", event.getUserId(), e);
        }
    }

    private NotificationType mapEventTypeToNotificationType(String eventType) {
        return switch (eventType) {
            case "USER_REGISTERED" -> NotificationType.WELCOME;
            case "USER_VERIFIED" -> NotificationType.ACCOUNT_VERIFIED;
            case "PASSWORD_RESET_REQUESTED" -> NotificationType.PASSWORD_RESET;
            default -> {
                log.warn("Unknown user event type: {}", eventType);
                yield null;
            }
        };
    }

    private String getSubject(NotificationType type, UserEvent event) {
        return switch (type) {
            case WELCOME -> "Welcome to E-Commerce, " + event.getFirstName() + "!";
            case ACCOUNT_VERIFIED -> "Your Account Has Been Verified";
            case PASSWORD_RESET -> "Password Reset Request";
            default -> "Account Update";
        };
    }

    private String generateContent(NotificationType type, UserEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");

        String greeting = event.getFirstName() != null
                ? "Dear " + event.getFirstName() + ","
                : "Hello,";
        sb.append("<p>").append(greeting).append("</p>");

        switch (type) {
            case WELCOME -> {
                sb.append("<h2>Welcome to E-Commerce!</h2>");
                sb.append("<p>Thank you for creating an account with us.</p>");
                sb.append("<p>You can now:</p>");
                sb.append("<ul>");
                sb.append("<li>Browse our wide selection of products</li>");
                sb.append("<li>Add items to your wishlist</li>");
                sb.append("<li>Track your orders</li>");
                sb.append("<li>Enjoy exclusive member discounts</li>");
                sb.append("</ul>");
                sb.append("<p>Happy shopping!</p>");
            }
            case ACCOUNT_VERIFIED -> {
                sb.append("<h2>Account Verified!</h2>");
                sb.append("<p>Your account has been successfully verified.</p>");
                sb.append("<p>You now have full access to all features of our platform.</p>");
            }
            case PASSWORD_RESET -> {
                sb.append("<h2>Password Reset Request</h2>");
                sb.append("<p>We received a request to reset your password.</p>");
                sb.append("<p>If you did not make this request, please ignore this email.</p>");
                sb.append("<p>Otherwise, click the link below to reset your password:</p>");
                sb.append("<p><a href='#'>Reset Password</a></p>");
                sb.append("<p>This link will expire in 24 hours.</p>");
            }
            default -> sb.append("<p>Account update notification.</p>");
        }

        sb.append("<p>Best regards,<br>E-Commerce Team</p>");
        sb.append("</body></html>");

        return sb.toString();
    }
}

