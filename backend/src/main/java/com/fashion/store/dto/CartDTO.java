package com.fashion.store.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CartDTO {

    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private Integer quantity;
        private String size;
        private String color;
        private BigDecimal lineTotal;

        public CartItemResponse() {}

        public CartItemResponse(Long id, Long productId, String productName, String productImage,
                                BigDecimal price, BigDecimal discountPrice, Integer quantity,
                                String size, String color, BigDecimal lineTotal) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.productImage = productImage;
            this.price = price;
            this.discountPrice = discountPrice;
            this.quantity = quantity;
            this.size = size;
            this.color = color;
            this.lineTotal = lineTotal;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public BigDecimal getDiscountPrice() { return discountPrice; }
        public void setDiscountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private Long productId;
            private String productName;
            private String productImage;
            private BigDecimal price;
            private BigDecimal discountPrice;
            private Integer quantity;
            private String size;
            private String color;
            private BigDecimal lineTotal;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder productId(Long productId) { this.productId = productId; return this; }
            public Builder productName(String productName) { this.productName = productName; return this; }
            public Builder productImage(String productImage) { this.productImage = productImage; return this; }
            public Builder price(BigDecimal price) { this.price = price; return this; }
            public Builder discountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; return this; }
            public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
            public Builder size(String size) { this.size = size; return this; }
            public Builder color(String color) { this.color = color; return this; }
            public Builder lineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; return this; }

            public CartItemResponse build() {
                return new CartItemResponse(id, productId, productName, productImage, price, discountPrice, quantity, size, color, lineTotal);
            }
        }
    }

    public static class CartResponse {
        private List<CartItemResponse> items;
        private int itemCount;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal total;

        public CartResponse() {}

        public CartResponse(List<CartItemResponse> items, int itemCount, BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
            this.items = items;
            this.itemCount = itemCount;
            this.subtotal = subtotal;
            this.discount = discount;
            this.total = total;
        }

        public List<CartItemResponse> getItems() { return items; }
        public void setItems(List<CartItemResponse> items) { this.items = items; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private List<CartItemResponse> items;
            private int itemCount;
            private BigDecimal subtotal;
            private BigDecimal discount;
            private BigDecimal total;

            public Builder items(List<CartItemResponse> items) { this.items = items; return this; }
            public Builder itemCount(int itemCount) { this.itemCount = itemCount; return this; }
            public Builder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
            public Builder discount(BigDecimal discount) { this.discount = discount; return this; }
            public Builder total(BigDecimal total) { this.total = total; return this; }

            public CartResponse build() {
                return new CartResponse(items, itemCount, subtotal, discount, total);
            }
        }
    }

    public static class AddToCartRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private String size;
        private String color;

        public AddToCartRequest() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class UpdateCartRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public UpdateCartRequest() {}

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
