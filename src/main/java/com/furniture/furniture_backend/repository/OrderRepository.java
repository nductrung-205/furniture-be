package com.furniture.furniture_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furniture.furniture_backend.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByCreatedAtBetweenAndPaymentStatus(
    LocalDateTime startDateTime, 
    LocalDateTime endDateTime, 
    Order.PaymentStatus paymentStatus
);
}