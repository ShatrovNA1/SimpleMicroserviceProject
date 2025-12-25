package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.PaymentRequest;
import com.ecommerce.orderservice.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.warn("Fallback: Unable to process payment for order: {}", request.getOrderId());
        return PaymentResponse.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .status("FAILED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.warn("Fallback: Unable to get payment for order: {}", orderId);
        return PaymentResponse.builder()
                .orderId(orderId)
                .amount(BigDecimal.ZERO)
                .status("UNKNOWN")
                .build();
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {
        log.warn("Fallback: Unable to refund payment: {}", paymentId);
        return PaymentResponse.builder()
                .id(paymentId)
                .status("REFUND_FAILED")
                .build();
    }
}

