package com.fashion.store.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private CategoryDTO.CategoryResponse category;
        private Integer stockQuantity;
        private List<String> imageUrls;
        private List<String> sizes;
        private List<String> colors;
        private Double rating;
        private Integer reviewCount;
        private String createdAt;
    }

    @Data
    public static class ProductRequest {
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Name too long")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;

        private BigDecimal discountPrice;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stockQuantity;

        private List<String> imageUrls;
        private List<String> sizes;
        private List<String> colors;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PagedProductResponse {
        private List<ProductResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
