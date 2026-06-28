package com.fashion.store.service.impl;

import com.fashion.store.dto.OrderDTO;
import com.fashion.store.dto.PaymentDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.*;
import com.fashion.store.repository.*;
import com.fashion.store.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public OrderDTO.OrderResponse checkout(Long userId, OrderDTO.CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items before checking out.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Build order items and calculate total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            }

            BigDecimal price = product.getDiscountPrice() != null
                    ? product.getDiscountPrice() : product.getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);

            OrderItem oi = OrderItem.builder()
                    .product(product)
                    .quantity(ci.getQuantity())
                    .priceAtPurchase(price)
                    .size(ci.getSize())
                    .color(ci.getColor())
                    .build();
            orderItems.add(oi);

            // Decrement stock
            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            productRepository.save(product);
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .paymentStatus("PENDING")
                .build();

        order = orderRepository.save(order);

        // Attach order reference and save items
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.getItems().addAll(orderItems);
        order = orderRepository.save(order);

        // Process mock payment
        PaymentDTO.PaymentInitiateRequest paymentRequest = PaymentDTO.PaymentInitiateRequest.builder()
                .orderId(order.getId())
                .paymentMethod(request.getPaymentMethod())
                .cardName(request.getCardName())
                .cardNumberLast4(request.getCardNumberLast4())
                .build();

        PaymentDTO.PaymentResponse paymentResult = paymentService.initiatePayment(paymentRequest);

        // Update order status based on payment result
        if ("SUCCESS".equals(paymentResult.getStatus())) {
            order.setStatus(Order.OrderStatus.PAID);
            order.setPaymentStatus("PAID");
        } else {
            order.setPaymentStatus("FAILED");
        }

        order = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAllByUserId(userId);

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO.OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO.OrderResponse getOrderById(Long orderId, Long userId) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        } else {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        }
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO.PagedOrderResponse getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<OrderDTO.OrderResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return OrderDTO.PagedOrderResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public OrderDTO.OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status +
                    ". Valid values: PENDING, PAID, SHIPPED, DELIVERED, CANCELLED");
        }
        return toResponse(orderRepository.save(order));
    }

    private OrderDTO.OrderResponse toResponse(Order order) {
        List<OrderDTO.OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        PaymentDTO.PaymentResponse paymentResponse = paymentRepository.findByOrderId(order.getId())
                .map(this::toPaymentResponse)
                .orElse(null);

        return OrderDTO.OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .items(items)
                .payment(paymentResponse)
                .build();
    }

    private OrderDTO.OrderItemResponse toItemResponse(OrderItem oi) {
        Product p = oi.getProduct();
        String imageUrl = null;
        if (StringUtils.hasText(p.getImageUrls())) {
            imageUrl = p.getImageUrls().split(",")[0].trim();
        }
        BigDecimal lineTotal = oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(oi.getQuantity()));

        return OrderDTO.OrderItemResponse.builder()
                .id(oi.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImage(imageUrl)
                .quantity(oi.getQuantity())
                .priceAtPurchase(oi.getPriceAtPurchase())
                .size(oi.getSize())
                .color(oi.getColor())
                .lineTotal(lineTotal)
                .build();
    }

    private PaymentDTO.PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentDTO.PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .build();
    }
}
