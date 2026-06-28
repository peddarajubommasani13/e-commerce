package com.fashion.store.service.impl;

import com.fashion.store.dto.PaymentDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.ResourceNotFoundException;
import com.fashion.store.repository.*;
import com.fashion.store.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Mock payment gateway implementation.
 * Simulates payment processing — always succeeds for valid orders.
 * To integrate a real provider (Stripe, Razorpay), replace this class
 * with a new implementation of PaymentService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MockPaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentDTO.PaymentResponse initiatePayment(PaymentDTO.PaymentInitiateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        // Simulate payment processing
        log.info("Processing mock payment for order {} via {}", order.getId(), request.getPaymentMethod());

        // Generate fake transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Simulate success (you could add failure logic based on test card numbers here)
        String status = "SUCCESS";
        String message = "Payment processed successfully (mock)";

        Payment payment = Payment.builder()
                .order(order)
                .provider(request.getPaymentMethod() != null ? request.getPaymentMethod() : "MOCK_GATEWAY")
                .transactionId(transactionId)
                .status(status)
                .amount(order.getTotalAmount())
                .build();

        payment = paymentRepository.save(payment);

        log.info("Mock payment completed: txn={}, status={}", transactionId, status);

        return PaymentDTO.PaymentResponse.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .provider(payment.getProvider())
                .transactionId(transactionId)
                .status(status)
                .amount(order.getTotalAmount())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO.PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order", orderId));

        return PaymentDTO.PaymentResponse.builder()
                .id(payment.getId())
                .orderId(orderId)
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .build();
    }
}
