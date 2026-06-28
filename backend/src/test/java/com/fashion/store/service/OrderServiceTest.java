package com.fashion.store.service;

import com.fashion.store.dto.OrderDTO;
import com.fashion.store.dto.PaymentDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.*;
import com.fashion.store.repository.*;
import com.fashion.store.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository    orderRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository  productRepository;
    @Mock private UserRepository     userRepository;
    @Mock private PaymentRepository  paymentRepository;
    @Mock private PaymentService     paymentService;

    @InjectMocks private OrderServiceImpl orderService;

    private User    testUser;
    private Product testProduct;
    private CartItem cartItem;
    private Order   testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).name("Jane Doe").email("jane@example.com")
                .role(User.Role.USER).build();

        testProduct = Product.builder()
                .id(1L).name("Test Dress")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .imageUrls("https://example.com/img.jpg")
                .build();

        cartItem = CartItem.builder()
                .id(1L).user(testUser).product(testProduct)
                .quantity(2).size("M").build();

        testOrder = Order.builder()
                .id(1L).user(testUser)
                .totalAmount(new BigDecimal("199.98"))
                .status(Order.OrderStatus.PAID)
                .shippingAddress("123 Luxury Ave, New York, NY 10001")
                .paymentStatus("PAID")
                .items(new ArrayList<>())
                .build();
    }

    // ——— Checkout ———

    @Test
    @DisplayName("Checkout: empty cart throws BadRequestException")
    void checkout_emptyCart_throws() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        OrderDTO.CheckoutRequest req = new OrderDTO.CheckoutRequest();
        req.setShippingAddress("123 Street");

        assertThatThrownBy(() -> orderService.checkout(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("Checkout: insufficient stock throws BadRequestException")
    void checkout_insufficientStock_throws() {
        testProduct.setStockQuantity(1);  // only 1 in stock
        cartItem.setQuantity(5);          // trying to buy 5

        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        OrderDTO.CheckoutRequest req = new OrderDTO.CheckoutRequest();
        req.setShippingAddress("123 Street");

        assertThatThrownBy(() -> orderService.checkout(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Checkout: successful flow returns OrderResponse with PAID status")
    void checkout_success() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // First save (pending), second save (after payment)
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            if (o.getItems() == null) o.setItems(new ArrayList<>());
            return o;
        });

        PaymentDTO.PaymentResponse paymentResponse = PaymentDTO.PaymentResponse.builder()
                .id(1L).status("SUCCESS").transactionId("TXN-001")
                .amount(new BigDecimal("199.98")).build();
        when(paymentService.initiatePayment(any())).thenReturn(paymentResponse);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        OrderDTO.CheckoutRequest req = new OrderDTO.CheckoutRequest();
        req.setShippingAddress("123 Luxury Ave, NY");

        OrderDTO.OrderResponse response = orderService.checkout(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PAID");
        assertThat(response.getPaymentStatus()).isEqualTo("PAID");
        verify(cartItemRepository).deleteAllByUserId(1L);
    }

    // ——— Get Orders ———

    @Test
    @DisplayName("GetUserOrders: returns list mapped to OrderResponse")
    void getUserOrders_success() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        List<OrderDTO.OrderResponse> orders = orderService.getUserOrders(1L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(1L);
        assertThat(orders.get(0).getTotalAmount()).isEqualByComparingTo(new BigDecimal("199.98"));
    }

    @Test
    @DisplayName("GetOrderById: valid userId returns order")
    void getOrderById_success() {
        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        OrderDTO.OrderResponse response = orderService.getOrderById(1L, 1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("GetOrderById: not found throws ResourceNotFoundException")
    void getOrderById_notFound() {
        when(orderRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ——— Admin: update status ———

    @Test
    @DisplayName("UpdateOrderStatus: valid status persists change")
    void updateOrderStatus_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        OrderDTO.OrderResponse response = orderService.updateOrderStatus(1L, "SHIPPED");

        verify(orderRepository).save(any(Order.class));
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("UpdateOrderStatus: invalid status throws BadRequestException")
    void updateOrderStatus_invalidStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid order status");
    }

    // ——— Admin: getAllOrders ———

    @Test
    @DisplayName("GetAllOrders: returns paged response")
    void getAllOrders_paged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        OrderDTO.PagedOrderResponse response = orderService.getAllOrders(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }
}
