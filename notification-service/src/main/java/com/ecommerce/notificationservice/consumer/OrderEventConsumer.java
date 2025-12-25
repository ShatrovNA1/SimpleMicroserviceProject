package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.dto.OrderNotificationRequest;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.event.OrderEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.order-events:order-events}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        try {
            NotificationType notificationType = mapEventTypeToNotificationType(event.getEventType());

            if (notificationType != null && event.getUserEmail() != null) {
                OrderNotificationRequest request = OrderNotificationRequest.builder()
                        .userId(event.getUserId())
                        .email(event.getUserEmail())
                        .orderNumber(event.getOrderNumber())
                        .totalAmount(event.getTotalAmount())
                        .shippingAddress(event.getShippingAddress())
                        .status(event.getStatus())
                        .items(event.getItems() != null ? event.getItems().stream()
                                .map(item -> OrderNotificationRequest.OrderItemDto.builder()
                                        .productName(item.getProductName())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice())
                                        .build())
                                .collect(Collectors.toList()) : null)
                        .build();

                notificationService.sendOrderNotification(request, notificationType);
                log.info("Order notification sent for orderId={}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error processing order event: orderId={}", event.getOrderId(), e);
        }
    }

    private NotificationType mapEventTypeToNotificationType(String eventType) {
        return switch (eventType) {
            case "ORDER_CREATED" -> NotificationType.ORDER_CREATED;
            case "ORDER_CONFIRMED" -> NotificationType.ORDER_CONFIRMED;
            case "ORDER_SHIPPED" -> NotificationType.ORDER_SHIPPED;
            case "ORDER_DELIVERED" -> NotificationType.ORDER_DELIVERED;
            case "ORDER_CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> {
                log.warn("Unknown order event type: {}", eventType);
                yield null;
            }
        };
    }
}

