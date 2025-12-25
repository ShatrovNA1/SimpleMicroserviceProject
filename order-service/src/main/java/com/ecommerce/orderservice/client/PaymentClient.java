package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.PaymentRequest;
import com.ecommerce.orderservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/api/payments")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);

    @GetMapping("/api/payments/order/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/payments/{paymentId}/refund")
    PaymentResponse refundPayment(@PathVariable("paymentId") Long paymentId);
}

