package com.furniture.furniture_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

import com.furniture.furniture_backend.entity.OrderItem;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String imageUrl;
    
    public static OrderItemResponse fromOrderItem(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());

        if (item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty()) {
            response.setImageUrl(item.getProduct().getImageUrls().get(0));
        }
        
        return response;
    }
}