package com.fashion.store.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminStatsDTO {
    private long totalOrders;
    private long totalUsers;
    private long totalProducts;
    private BigDecimal totalRevenue;
}
