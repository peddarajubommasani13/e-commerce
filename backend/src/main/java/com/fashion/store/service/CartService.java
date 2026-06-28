package com.fashion.store.service;

import com.fashion.store.dto.CartDTO;

public interface CartService {
    CartDTO.CartResponse getCart(Long userId);
    CartDTO.CartResponse addToCart(Long userId, CartDTO.AddToCartRequest request);
    CartDTO.CartResponse updateCartItem(Long userId, Long itemId, CartDTO.UpdateCartRequest request);
    CartDTO.CartResponse removeCartItem(Long userId, Long itemId);
    void clearCart(Long userId);
}
