package com.fashion.store.controller;

import com.fashion.store.dto.PaymentDTO;
import com.fashion.store.entity.User;
import com.fashion.store.repository.UserRepository;
import com.fashion.store.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentDTO.PaymentResponse> initiatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentDTO.PaymentInitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDTO.PaymentResponse> getPaymentByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
