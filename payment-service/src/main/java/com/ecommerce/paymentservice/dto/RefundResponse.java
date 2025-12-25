package com.ecommerce.paymentservice.dto;

import com.ecommerce.paymentservice.entity.RefundStatus;
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
public class RefundResponse {

    private Long id;
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}

