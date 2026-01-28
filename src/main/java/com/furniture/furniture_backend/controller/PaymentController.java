package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay/create-url/{orderId}")
    public ResponseEntity<ApiResponse<String>> createVNPayUrl(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createVNPayPayment(orderId, request);
            return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công", paymentUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}