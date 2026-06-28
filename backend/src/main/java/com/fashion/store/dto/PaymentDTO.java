package com.fashion.store.dto;

import lombok.*;
import java.math.BigDecimal;

public class PaymentDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentInitiateRequest {
        private Long orderId;
        private String paymentMethod;
        private String cardName;
        private String cardNumberLast4;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private Long orderId;
        private String provider;
        private String transactionId;
        private String status;
        private BigDecimal amount;
        private String createdAt;
        private String message;
    }
}
