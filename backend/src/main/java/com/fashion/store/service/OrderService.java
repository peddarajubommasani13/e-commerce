package com.fashion.store.service;

import com.fashion.store.dto.OrderDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO.OrderResponse checkout(Long userId, OrderDTO.CheckoutRequest request);
    List<OrderDTO.OrderResponse> getUserOrders(Long userId);
    OrderDTO.OrderResponse getOrderById(Long orderId, Long userId);
    OrderDTO.PagedOrderResponse getAllOrders(Pageable pageable);
    OrderDTO.OrderResponse updateOrderStatus(Long orderId, String status);
}
