package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.*;
import com.ecommerce.paymentservice.entity.*;
import com.ecommerce.paymentservice.exception.*;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGatewaySimulator paymentGateway;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Проверка на дубликат платежа
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            Payment existingPayment = paymentRepository.findByOrderId(request.getOrderId())
                    .orElseThrow();
            if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
                throw new DuplicatePaymentException("Payment already completed for order: " + request.getOrderId());
            }
        }

        // Создание записи платежа
        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(method)
                .status(PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);

        // Обработка через платёжный шлюз
        boolean success = paymentGateway.processPayment(
                request.getPaymentMethod(),
                request.getAmount().doubleValue()
        );

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(paymentGateway.generateTransactionId());
            payment.setProcessedAt(LocalDateTime.now());
            log.info("Payment completed successfully: {}", payment.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by payment gateway");
            log.warn("Payment failed for order: {}", request.getOrderId());
        }

        payment = paymentRepository.save(payment);
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction: " + transactionId));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(this::mapToPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable)
                .map(this::mapToPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(this::mapToPaymentResponse);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        return refundPayment(paymentId, null);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException("Can only refund completed payments");
        }

        BigDecimal refundAmount = (request != null && request.getAmount() != null)
                ? request.getAmount()
                : payment.getAmount();

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentStateException("Refund amount cannot exceed payment amount");
        }

        // Создание записи возврата
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(refundAmount)
                .reason(request != null ? request.getReason() : "Customer requested refund")
                .status(RefundStatus.PROCESSING)
                .build();

        refund = refundRepository.save(refund);

        // Обработка возврата через платёжный шлюз
        boolean success = paymentGateway.processRefund(
                payment.getTransactionId(),
                refundAmount.doubleValue()
        );

        if (success) {
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setTransactionId(paymentGateway.generateTransactionId());
            refund.setProcessedAt(LocalDateTime.now());

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundId(refund.getId());

            log.info("Refund completed successfully: {}", refund.getTransactionId());
        } else {
            refund.setStatus(RefundStatus.FAILED);
            log.warn("Refund failed for payment: {}", paymentId);
            throw new PaymentProcessingException("Refund processing failed");
        }

        refundRepository.save(refund);
        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId).stream()
                .map(this::mapToRefundResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException("Can only cancel pending or processing payments");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        log.info("Payment cancelled: {}", paymentId);
        return mapToPaymentResponse(payment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    private RefundResponse mapToRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment().getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .transactionId(refund.getTransactionId())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}

