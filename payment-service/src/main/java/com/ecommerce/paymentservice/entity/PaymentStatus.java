package com.ecommerce.paymentservice.entity;

public enum PaymentStatus {
    PENDING,      // Ожидает обработки
    PROCESSING,   // В процессе обработки
    COMPLETED,    // Успешно завершён
    FAILED,       // Ошибка платежа
    REFUNDED,     // Возвращён
    CANCELLED     // Отменён
}

