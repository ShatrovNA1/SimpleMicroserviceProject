package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.dto.SendNotificationRequest;
import com.ecommerce.notificationservice.entity.NotificationChannel;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.event.PaymentEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.payment-events:payment-events}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: type={}, paymentId={}", event.getEventType(), event.getPaymentId());

        try {
            NotificationType notificationType = mapEventTypeToNotificationType(event.getEventType());

            if (notificationType != null && event.getUserEmail() != null) {
                String subject = getSubject(notificationType, event);
                String content = generateContent(notificationType, event);

                SendNotificationRequest request = SendNotificationRequest.builder()
                        .userId(event.getUserId())
                        .recipient(event.getUserEmail())
                        .type(notificationType)
                        .channel(NotificationChannel.EMAIL)
                        .subject(subject)
                        .content(content)
                        .referenceId(event.getTransactionId())
                        .referenceType("PAYMENT")
                        .build();

                notificationService.sendNotification(request);
                log.info("Payment notification sent for paymentId={}", event.getPaymentId());
            }
        } catch (Exception e) {
            log.error("Error processing payment event: paymentId={}", event.getPaymentId(), e);
        }
    }

    private NotificationType mapEventTypeToNotificationType(String eventType) {
        return switch (eventType) {
            case "PAYMENT_COMPLETED" -> NotificationType.PAYMENT_SUCCESS;
            case "PAYMENT_FAILED" -> NotificationType.PAYMENT_FAILED;
            case "PAYMENT_REFUNDED" -> NotificationType.PAYMENT_REFUNDED;
            default -> {
                log.warn("Unknown payment event type: {}", eventType);
                yield null;
            }
        };
    }

    private String getSubject(NotificationType type, PaymentEvent event) {
        return switch (type) {
            case PAYMENT_SUCCESS -> "Payment Successful - Transaction #" + event.getTransactionId();
            case PAYMENT_FAILED -> "Payment Failed - Order #" + event.getOrderId();
            case PAYMENT_REFUNDED -> "Payment Refunded - Transaction #" + event.getTransactionId();
            default -> "Payment Update";
        };
    }

    private String generateContent(NotificationType type, PaymentEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");

        switch (type) {
            case PAYMENT_SUCCESS -> {
                sb.append("<h2>Payment Successful!</h2>");
                sb.append("<p>Your payment has been processed successfully.</p>");
                sb.append("<p><strong>Transaction ID:</strong> ").append(event.getTransactionId()).append("</p>");
                sb.append("<p><strong>Amount:</strong> ").append(event.getCurrency()).append(" ").append(event.getAmount()).append("</p>");
                sb.append("<p><strong>Payment Method:</strong> ").append(event.getPaymentMethod()).append("</p>");
            }
            case PAYMENT_FAILED -> {
                sb.append("<h2>Payment Failed</h2>");
                sb.append("<p>Unfortunately, your payment could not be processed.</p>");
                sb.append("<p><strong>Amount:</strong> ").append(event.getCurrency()).append(" ").append(event.getAmount()).append("</p>");
                if (event.getFailureReason() != null) {
                    sb.append("<p><strong>Reason:</strong> ").append(event.getFailureReason()).append("</p>");
                }
                sb.append("<p>Please try again or use a different payment method.</p>");
            }
            case PAYMENT_REFUNDED -> {
                sb.append("<h2>Payment Refunded</h2>");
                sb.append("<p>Your payment has been refunded.</p>");
                sb.append("<p><strong>Transaction ID:</strong> ").append(event.getTransactionId()).append("</p>");
                sb.append("<p><strong>Refund Amount:</strong> ").append(event.getCurrency()).append(" ").append(event.getAmount()).append("</p>");
                sb.append("<p>The amount will be credited to your original payment method within 5-10 business days.</p>");
            }
            default -> sb.append("<p>Payment update notification.</p>");
        }

        sb.append("<p>Thank you for shopping with us!</p>");
        sb.append("</body></html>");

        return sb.toString();
    }
}

