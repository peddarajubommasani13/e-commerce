package com.fashion.store.controller;

import com.fashion.store.dto.CartDTO;
import com.fashion.store.entity.User;
import com.fashion.store.repository.UserRepository;
import com.fashion.store.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<CartDTO.CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(resolveUserId(userDetails)));
    }

    @PostMapping
    public ResponseEntity<CartDTO.CartResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartDTO.AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(resolveUserId(userDetails), request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartDTO.CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody CartDTO.UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(resolveUserId(userDetails), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartDTO.CartResponse> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(resolveUserId(userDetails), itemId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
