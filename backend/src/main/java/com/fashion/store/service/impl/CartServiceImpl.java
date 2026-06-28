package com.fashion.store.service.impl;

import com.fashion.store.dto.CartDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.*;
import com.fashion.store.repository.*;
import com.fashion.store.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartDTO.CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return buildCartResponse(items);
    }

    @Override
    @Transactional
    public CartDTO.CartResponse addToCart(Long userId, CartDTO.AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        String size = request.getSize();
        String color = request.getColor();

        // Check if item with same product+size+color already exists
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductIdAndSizeAndColor(
                userId, request.getProductId(), size, color);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            CartItem item = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .size(size)
                    .color(color)
                    .build();
            cartItemRepository.save(item);
        }

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartDTO.CartResponse updateCartItem(Long userId, Long itemId, CartDTO.UpdateCartRequest request) {
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));

        Product product = item.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartDTO.CartResponse removeCartItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        cartItemRepository.delete(item);
        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }

    private CartDTO.CartResponse buildCartResponse(List<CartItem> items) {
        List<CartDTO.CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = itemResponses.stream()
                .filter(i -> i.getDiscountPrice() != null)
                .map(i -> i.getPrice().subtract(i.getDiscountPrice()).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = itemResponses.stream()
                .map(CartDTO.CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.CartResponse.builder()
                .items(itemResponses)
                .itemCount(items.size())
                .subtotal(subtotal)
                .discount(discount)
                .total(total)
                .build();
    }

    private CartDTO.CartItemResponse toItemResponse(CartItem item) {
        Product p = item.getProduct();
        BigDecimal effectivePrice = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice();
        BigDecimal lineTotal = effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        String imageUrl = null;
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            imageUrl = p.getImageUrls().split(",")[0].trim();
        }

        return CartDTO.CartItemResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImage(imageUrl)
                .price(p.getPrice())
                .discountPrice(p.getDiscountPrice())
                .quantity(item.getQuantity())
                .size(item.getSize())
                .color(item.getColor())
                .lineTotal(lineTotal)
                .build();
    }
}
