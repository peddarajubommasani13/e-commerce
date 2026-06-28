package com.fashion.store.service;

import com.fashion.store.dto.PaymentDTO;

public interface PaymentService {
    PaymentDTO.PaymentResponse initiatePayment(PaymentDTO.PaymentInitiateRequest request);
    PaymentDTO.PaymentResponse getPaymentByOrderId(Long orderId);
}
