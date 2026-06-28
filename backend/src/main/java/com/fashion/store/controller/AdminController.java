package com.fashion.store.controller;

import com.fashion.store.dto.AdminStatsDTO;
import com.fashion.store.dto.OrderDTO;
import com.fashion.store.dto.ProductDTO;
import com.fashion.store.repository.*;
import com.fashion.store.service.OrderService;
import com.fashion.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @GetMapping("/orders")
    public ResponseEntity<OrderDTO.PagedOrderResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO.OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderDTO.UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        AdminStatsDTO stats = AdminStatsDTO.builder()
                .totalOrders(orderRepository.countAllOrders())
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalRevenue(orderRepository.sumTotalRevenue())
                .build();
        return ResponseEntity.ok(stats);
    }
}
