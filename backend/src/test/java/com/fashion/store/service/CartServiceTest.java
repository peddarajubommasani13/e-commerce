package com.fashion.store.service;

import com.fashion.store.dto.CartDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.*;
import com.fashion.store.repository.*;
import com.fashion.store.service.impl.CartServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test").email("test@example.com").build();
        testProduct = Product.builder()
                .id(1L).name("Dress").price(new BigDecimal("99.99"))
                .stockQuantity(10).imageUrls("img.jpg").build();
        testCartItem = CartItem.builder()
                .id(1L).user(testUser).product(testProduct).quantity(2).size("M").build();
    }

    @Test
    @DisplayName("GetCart: returns cart with calculated totals")
    void getCart_success() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(testCartItem));

        CartDTO.CartResponse cart = cartService.getCart(1L);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getTotal()).isEqualByComparingTo(new BigDecimal("199.98"));
        assertThat(cart.getItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("AddToCart: new item is saved")
    void addToCart_newItem() {
        CartDTO.AddToCartRequest req = new CartDTO.AddToCartRequest();
        req.setProductId(1L);
        req.setQuantity(1);
        req.setSize("M");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductIdAndSizeAndColor(1L, 1L, "M", null))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(testCartItem));

        CartDTO.CartResponse cart = cartService.addToCart(1L, req);

        verify(cartItemRepository).save(any(CartItem.class));
        assertThat(cart.getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("AddToCart: out of stock throws BadRequestException")
    void addToCart_outOfStock() {
        testProduct.setStockQuantity(0);
        CartDTO.AddToCartRequest req = new CartDTO.AddToCartRequest();
        req.setProductId(1L);
        req.setQuantity(5);
        req.setSize("M");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addToCart(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("RemoveCartItem: existing item is deleted")
    void removeCartItem_success() {
        when(cartItemRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        CartDTO.CartResponse cart = cartService.removeCartItem(1L, 1L);

        verify(cartItemRepository).delete(testCartItem);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("RemoveCartItem: wrong user throws ResourceNotFoundException")
    void removeCartItem_wrongUser() {
        when(cartItemRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeCartItem(2L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
