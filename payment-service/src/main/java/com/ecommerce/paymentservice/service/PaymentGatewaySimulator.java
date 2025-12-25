package com.ecommerce.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * Симулятор платёжного шлюза.
 * В реальном проекте здесь была бы интеграция со Stripe, PayPal и т.д.
 */
@Service
@Slf4j
public class PaymentGatewaySimulator {

    @Value("${payment.simulation.success-rate:0.95}")
    private double successRate;

    @Value("${payment.simulation.processing-delay:1000}")
    private long processingDelay;

    private final Random random = new Random();

    /**
     * Имитирует обработку платежа
     * @return true если платёж успешен, false если неудачен
     */
    public boolean processPayment(String paymentMethod, double amount) {
        log.info("Processing payment: method={}, amount={}", paymentMethod, amount);

        // Имитация задержки обработки
        try {
            Thread.sleep(processingDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Имитация результата на основе вероятности успеха
        boolean success = random.nextDouble() < successRate;

        if (success) {
            log.info("Payment processed successfully");
        } else {
            log.warn("Payment processing failed (simulated failure)");
        }

        return success;
    }

    /**
     * Генерирует уникальный ID транзакции
     */
    public String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().toUpperCase().substring(0, 12);
    }

    /**
     * Имитирует обработку возврата
     */
    public boolean processRefund(String transactionId, double amount) {
        log.info("Processing refund: transactionId={}, amount={}", transactionId, amount);

        try {
            Thread.sleep(processingDelay / 2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Возвраты обычно имеют более высокий процент успеха
        boolean success = random.nextDouble() < 0.99;

        if (success) {
            log.info("Refund processed successfully");
        } else {
            log.warn("Refund processing failed");
        }

        return success;
    }

    /**
     * Валидация данных карты (имитация)
     */
    public boolean validateCard(String cardNumber, String cvv, String expiryDate) {
        // Простая валидация формата (в реальности это делает платёжный шлюз)
        return cardNumber != null && cardNumber.length() >= 13 && cardNumber.length() <= 19;
    }
}

