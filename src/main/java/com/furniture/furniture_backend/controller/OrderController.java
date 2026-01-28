package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.OrderRequest;
import com.furniture.furniture_backend.dto.OrderResponse;
import com.furniture.furniture_backend.dto.PageResponse;
import com.furniture.furniture_backend.entity.Order;
import com.furniture.furniture_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {
    
    private final OrderService orderService;
    
    // ========== ENDPOINTS CƠ BẢN ==========
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
            ApiResponse.success("Orders retrieved successfully", orders)
        );
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderResponse> orders = orderService.getAllOrdersPaginated(page, size);
        return ResponseEntity.ok(
            ApiResponse.success("Orders retrieved successfully", orders)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Order retrieved successfully", order)
        );
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(
            @PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(
            ApiResponse.success("User orders retrieved successfully", orders)
        );
    }
    
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(
            @PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(
            ApiResponse.success("Order retrieved successfully", order)
        );
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(
            ApiResponse.success("Orders by status retrieved successfully", orders)
        );
    }
    
    // ========== TẠO VÀ CẬP NHẬT ĐƠN HÀNG ==========
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable Long userId,
            @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Order created successfully", order)
        );
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Order.OrderStatus status = Order.OrderStatus.valueOf(request.get("status"));
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(
            ApiResponse.success("Order status updated successfully", order)
        );
    }
    
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(request.get("paymentStatus"));
        OrderResponse order = orderService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(
            ApiResponse.success("Payment status updated successfully", order)
        );
    }
    
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(
            ApiResponse.success("Order cancelled successfully", null)
        );
    }
}