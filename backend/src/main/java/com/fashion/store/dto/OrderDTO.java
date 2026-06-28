package com.fashion.store.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal priceAtPurchase;
        private String size;
        private String color;
        private BigDecimal lineTotal;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private Long id;
        private Long userId;
        private String userName;
        private BigDecimal totalAmount;
        private String status;
        private String shippingAddress;
        private String paymentStatus;
        private String createdAt;
        private List<OrderItemResponse> items;
        private PaymentDTO.PaymentResponse payment;
    }

    @Data
    public static class CheckoutRequest {
        @NotBlank(message = "Shipping address is required")
        private String shippingAddress;

        private String paymentMethod = "MOCK_GATEWAY";

        // Optional card details (mock — never store real data)
        private String cardName;
        private String cardNumberLast4;
    }

    @Data
    public static class UpdateOrderStatusRequest {
        @NotBlank(message = "Status is required")
        private String status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PagedOrderResponse {
        private List<OrderResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
