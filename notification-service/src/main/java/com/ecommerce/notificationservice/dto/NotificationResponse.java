package com.ecommerce.notificationservice.dto;

import com.ecommerce.notificationservice.entity.NotificationChannel;
import com.ecommerce.notificationservice.entity.NotificationStatus;
import com.ecommerce.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String recipient;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String content;
    private NotificationStatus status;
    private String errorMessage;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer retryCount;
}

