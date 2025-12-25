package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.NotificationResponse;
import com.ecommerce.notificationservice.dto.NotificationStatsResponse;
import com.ecommerce.notificationservice.dto.OrderNotificationRequest;
import com.ecommerce.notificationservice.dto.SendNotificationRequest;
import com.ecommerce.notificationservice.entity.*;
import com.ecommerce.notificationservice.exception.NotificationException;
import com.ecommerce.notificationservice.exception.ResourceNotFoundException;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending notification: type={}, channel={}, recipient={}",
                request.getType(), request.getChannel(), request.getRecipient());

        String subject = request.getSubject() != null ? request.getSubject() : getDefaultSubject(request.getType());
        String content = request.getContent() != null ? request.getContent() : generateContent(request);

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .recipient(request.getRecipient())
                .type(request.getType())
                .channel(request.getChannel())
                .subject(subject)
                .content(content)
                .status(NotificationStatus.PENDING)
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .build();

        notification = notificationRepository.save(notification);

        // Асинхронная отправка
        sendAsync(notification);

        return mapToResponse(notification);
    }

    @Async
    protected void sendAsync(Notification notification) {
        try {
            notification.setStatus(NotificationStatus.SENDING);
            notificationRepository.save(notification);

            switch (notification.getChannel()) {
                case EMAIL -> emailService.sendEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getContent()
                );
                case SMS -> smsService.sendSms(
                        notification.getRecipient(),
                        notification.getContent()
                );
                case PUSH -> log.info("Push notification would be sent to: {}", notification.getRecipient());
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Notification sent successfully: id={}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification: id={}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }

        notificationRepository.save(notification);
    }

    @Transactional
    public NotificationResponse sendOrderNotification(OrderNotificationRequest request, NotificationType type) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("orderNumber", request.getOrderNumber());
        templateData.put("totalAmount", request.getTotalAmount());
        templateData.put("shippingAddress", request.getShippingAddress());
        templateData.put("items", request.getItems());
        templateData.put("status", request.getStatus());

        String subject = switch (type) {
            case ORDER_CREATED -> "Order Confirmation - " + request.getOrderNumber();
            case ORDER_SHIPPED -> "Your Order Has Been Shipped - " + request.getOrderNumber();
            case ORDER_DELIVERED -> "Your Order Has Been Delivered - " + request.getOrderNumber();
            case ORDER_CANCELLED -> "Order Cancelled - " + request.getOrderNumber();
            default -> "Order Update - " + request.getOrderNumber();
        };

        String content = generateOrderEmailContent(request, type);

        SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                .userId(request.getUserId())
                .recipient(request.getEmail())
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .subject(subject)
                .content(content)
                .templateData(templateData)
                .referenceId(request.getOrderNumber())
                .referenceType("ORDER")
                .build();

        return sendNotification(notificationRequest);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public NotificationResponse retryNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new NotificationException("Can only retry failed notifications");
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setErrorMessage(null);
        notification = notificationRepository.save(notification);

        sendAsync(notification);

        return mapToResponse(notification);
    }

    @Transactional
    public void cancelNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new NotificationException("Cannot cancel already sent notification");
        }

        notification.setStatus(NotificationStatus.CANCELLED);
        notificationRepository.save(notification);
    }

    private String getDefaultSubject(NotificationType type) {
        return switch (type) {
            case WELCOME -> "Welcome to E-Commerce!";
            case ORDER_CREATED -> "Order Confirmation";
            case ORDER_CONFIRMED -> "Order Confirmed";
            case ORDER_SHIPPED -> "Your Order Has Been Shipped";
            case ORDER_DELIVERED -> "Your Order Has Been Delivered";
            case ORDER_CANCELLED -> "Order Cancelled";
            case PAYMENT_SUCCESS -> "Payment Successful";
            case PAYMENT_FAILED -> "Payment Failed";
            case PAYMENT_REFUNDED -> "Payment Refunded";
            case PASSWORD_RESET -> "Password Reset Request";
            case ACCOUNT_VERIFIED -> "Account Verified";
            case PROMOTIONAL -> "Special Offer";
        };
    }

    private String generateContent(SendNotificationRequest request) {
        if (request.getTemplateData() != null && !request.getTemplateData().isEmpty()) {
            // Здесь можно использовать Thymeleaf шаблоны
            return "Notification content with template data";
        }
        return "Default notification content for " + request.getType();
    }

    private String generateOrderEmailContent(OrderNotificationRequest request, NotificationType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>").append(getDefaultSubject(type)).append("</h2>");
        sb.append("<p>Order Number: <strong>").append(request.getOrderNumber()).append("</strong></p>");
        sb.append("<p>Total Amount: <strong>$").append(request.getTotalAmount()).append("</strong></p>");

        if (request.getShippingAddress() != null) {
            sb.append("<p>Shipping Address: ").append(request.getShippingAddress()).append("</p>");
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            sb.append("<h3>Order Items:</h3>");
            sb.append("<ul>");
            for (OrderNotificationRequest.OrderItemDto item : request.getItems()) {
                sb.append("<li>").append(item.getProductName())
                        .append(" x ").append(item.getQuantity())
                        .append(" - $").append(item.getPrice())
                        .append("</li>");
            }
            sb.append("</ul>");
        }

        sb.append("<p>Thank you for shopping with us!</p>");
        sb.append("</body></html>");

        return sb.toString();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .recipient(notification.getRecipient())
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats() {
        return NotificationStatsResponse.builder()
                .totalNotifications(notificationRepository.count())
                .pendingCount(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sendingCount(notificationRepository.countByStatus(NotificationStatus.SENDING))
                .sentCount(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failedCount(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .cancelledCount(notificationRepository.countByStatus(NotificationStatus.CANCELLED))
                .build();
    }
}

