package com.furniture.furniture_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.furniture.furniture_backend.entity.Order;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;
    private String note;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    
    public static OrderResponse fromOrder(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setRecipientName(order.getRecipientName());
        response.setRecipientPhone(order.getRecipientPhone());
        response.setNote(order.getNote());
        response.setItems(order.getOrderItems().stream()
                .map(OrderItemResponse::fromOrderItem)
                .collect(Collectors.toList()));
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}