package com.ecommerce.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private String transactionId;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String failureReason;
    private LocalDateTime timestamp;
}

