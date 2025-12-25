package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.Refund;
import com.ecommerce.paymentservice.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentId(Long paymentId);

    Optional<Refund> findByTransactionId(String transactionId);

    List<Refund> findByStatus(RefundStatus status);
}

