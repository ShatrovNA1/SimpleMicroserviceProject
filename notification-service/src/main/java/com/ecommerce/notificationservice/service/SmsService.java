package com.ecommerce.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Async
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS sending is disabled. Would send to: {}, message: {}", phoneNumber, message);
            return;
        }

        // В реальном проекте здесь была бы интеграция с SMS провайдером (Twilio, AWS SNS, etc.)
        log.info("Simulating SMS send to: {}, message: {}", phoneNumber, message);

        // Симуляция отправки
        try {
            Thread.sleep(500);
            log.info("SMS sent successfully to: {}", phoneNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("SMS sending interrupted for: {}", phoneNumber);
        }
    }
}

