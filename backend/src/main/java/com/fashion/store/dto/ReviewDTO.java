package com.fashion.store.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

public class ReviewDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReviewResponse {
        private Long id;
        private Long productId;
        private Long userId;
        private String userName;
        private Integer rating;
        private String comment;
        private String createdAt;
    }

    @Data
    public static class ReviewRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        private Integer rating;

        @Size(max = 2000, message = "Comment too long")
        private String comment;
    }
}
