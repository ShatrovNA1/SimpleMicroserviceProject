package com.ecommerce.orderservice.entity;

public enum OrderStatus {
    PENDING,        // Заказ создан, ожидает оплаты
    PAID,           // Оплачен
    PROCESSING,     // В обработке
    SHIPPED,        // Отправлен
    DELIVERED,      // Доставлен
    CANCELLED,      // Отменён
    REFUNDED        // Возвращён
}

