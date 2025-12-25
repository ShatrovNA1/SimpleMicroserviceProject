package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.PaymentClient;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.PaymentFailedException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // Получить информацию о продуктах
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        List<ProductResponse> products = productClient.getProductsByIds(productIds);
        Map<Long, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));

        // Проверить доступность и зарезервировать товары
        List<OrderItem> reservedItems = new ArrayList<>();
        try {
            for (OrderItemRequest itemRequest : request.getItems()) {
                ProductResponse product = productMap.get(itemRequest.getProductId());
                if (product == null || !product.isActive()) {
                    throw new ResourceNotFoundException("Product not found or inactive: " + itemRequest.getProductId());
                }

                Boolean reserved = productClient.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());
                if (reserved == null || !reserved) {
                    throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
                }

                OrderItem orderItem = OrderItem.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .productSku(product.getSku())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(product.getPrice())
                        .subtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                        .build();

                reservedItems.add(orderItem);
            }
        } catch (Exception e) {
            // Откатить резервирования при ошибке
            for (OrderItem item : reservedItems) {
                try {
                    productClient.releaseStock(item.getProductId(), item.getQuantity());
                } catch (Exception ex) {
                    log.error("Failed to release stock for product: {}", item.getProductId(), ex);
                }
            }
            throw e;
        }

        // Создать заказ
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItem item : reservedItems) {
            order.addItem(item);
        }

        order.calculateTotalAmount();
        order = orderRepository.save(order);

        log.info("Order created successfully: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        log.info("Order {} status updated to {}", order.getOrderNumber(), newStatus);
        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse processPayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order is not in PENDING status");
        }

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .build();

        PaymentResponse paymentResponse = paymentClient.processPayment(paymentRequest);

        if ("COMPLETED".equals(paymentResponse.getStatus())) {
            order.setPaymentId(paymentResponse.getId());
            order.setStatus(OrderStatus.PAID);
            order = orderRepository.save(order);
            log.info("Payment processed successfully for order: {}", order.getOrderNumber());
        } else {
            throw new PaymentFailedException("Payment failed for order: " + order.getOrderNumber());
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel order in " + order.getStatus() + " status");
        }

        // Освободить резервы товаров
        for (OrderItem item : order.getItems()) {
            try {
                productClient.releaseStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to release stock for product: {}", item.getProductId(), e);
            }
        }

        // Если был платёж - сделать возврат
        if (order.getPaymentId() != null) {
            try {
                paymentClient.refundPayment(order.getPaymentId());
                order.setStatus(OrderStatus.REFUNDED);
            } catch (Exception e) {
                log.error("Failed to refund payment for order: {}", order.getOrderNumber(), e);
                order.setStatus(OrderStatus.CANCELLED);
            }
        } else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        order = orderRepository.save(order);
        log.info("Order {} cancelled", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.REFUNDED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED, REFUNDED -> false;
        };

        if (!valid) {
            throw new InvalidOrderStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productSku(item.getProductSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentId(order.getPaymentId())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

