package com.furniture.furniture_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import com.furniture.furniture_backend.entity.Order;

@Data
public class OrderRequest {
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Payment method is required")
    private Order.PaymentMethod paymentMethod;
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    @NotBlank(message = "Recipient name is required")
    private String recipientName;
    
    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;
    
    private String note;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}